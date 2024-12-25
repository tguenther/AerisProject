package com.project.Aeris.AerisProject;

/// Java classes for handling exceptions
import java.io.IOException;
import java.nio.file.Paths;

/// Spring Boot classes
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/// Classes for handling lists
import com.google.common.collect.ImmutableList;

/// Classes for NetCDF file handling
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

/// Classes for image generation
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

/// Classes for file handling
import java.nio.file.Files;

/// Classes for JSON handling
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

/// Class to store concentration data.
/// This class is used to store the concentration data for a given time and z index.
/// The data can be formatted for HTML display or converted to an image.
/// The data is stored as a 2D array of double values.
/// The class also stores the time and z indices for reference.
class ConcentrationData {

    class MetaData{
        public double maxDataValue;
        public double minDataValue;
        public double dataRange;

        public void calculateMetaData(double[][] data) {
            maxDataValue = data[0][0];
            minDataValue = data[0][0];
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    if (data[i][j] > maxDataValue) {
                        maxDataValue = data[i][j];
                    }
                    if (data[i][j] < minDataValue) {
                        minDataValue = data[i][j];
                    }
                }
            }
            dataRange = maxDataValue - minDataValue;
        }
    }

    class TimeSeriesData {
        public int timeIndex;
        public int zIndex;
        public double[][] data;
    }

    private TimeSeriesData timeSeriesData;
    private MetaData metaData;

    public ConcentrationData(int timeIndex, int zIndex, double[][] data) {
        
        timeSeriesData = new TimeSeriesData();
        metaData = new MetaData();

        timeSeriesData.timeIndex = timeIndex;
        timeSeriesData.zIndex = zIndex;
        timeSeriesData.data = data;
        metaData.calculateMetaData(data);
    }

    public String generateHtml() {
        String html = "<table>";
        for (int i = 0; i < timeSeriesData.data.length; i++) {
            html += "<tr>";
            for (int j = 0; j < timeSeriesData.data[i].length; j++) {
                html += "<td>" + timeSeriesData.data[i][j] + "</td>";
            }
            html += "</tr>";
        }
        html += "</table>";
        return html;
    }

    public byte[] generateImage() throws IOException {
        
        final int scaleFactor = 20;

        int dataHeight = timeSeriesData.data.length;
        int dataWidth = timeSeriesData.data[0].length;

        int width = scaleFactor * dataWidth, height = scaleFactor * dataHeight;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        // Set background color
        graphics.setPaint(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
    
        // Draw the data (linearly scaled to [0-255])
        for (int i = 0; i < timeSeriesData.data.length; i++) {
            for (int j = 0; j < timeSeriesData.data[i].length; j++) {
                double value = timeSeriesData.data[i][j];
                int colorValue = (int) (255 * (value - metaData.minDataValue) / metaData.dataRange);
                graphics.setPaint(new Color(colorValue, colorValue, colorValue));
                graphics.fillRect(j * scaleFactor, i * scaleFactor, scaleFactor, scaleFactor);
            }
        }

        graphics.dispose();

        // Save image to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    public String toJsonString() {
        String json = "";
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(timeSeriesData);
        }
        catch (JsonProcessingException e)
        {

        }
        return json;
    }

    public int getTimeIndex() {
        return timeSeriesData.timeIndex;
    }

    public int getZIndex() {
        return timeSeriesData.zIndex;
    }

    public double[][] getData() {
        return timeSeriesData.data;
    }
}

/// Class to handle NetCDF files.
/// This is a wrapper around the NetCDF Java library.
class NetCDF {
	private NetcdfFile ncFile;

    /// Constructor
    /// Loads the NetCDF file from the given file path.
    /// @param filePath The path to the NetCDF file
	public NetCDF(String filePath) {
		try {
			ncFile = ucar.nc2.NetcdfFiles.open(filePath);
		} catch (IOException e) {
			System.out.println("Error loading NetCDF file: " + e.getMessage());
		}
	}

    /// Close the NetCDF file.
	public void close() {
		try {
			ncFile.close();
		} catch (IOException e) {
			System.out.println("Error closing NetCDF file: " + e.getMessage());
		}
	}

    /// Get the dimensions of the NetCDF file.
    /// @return The dimensions of the NetCDF file
	@SuppressWarnings("deprecation")
	public ImmutableList<Dimension> getDimensions() {
		return ncFile.getDimensions();
	}

    /// Get the variables of the NetCDF file.
    /// @return The variables of the NetCDF file
	public ImmutableList<Variable> getVariables() {
		return ncFile.getVariables();
	}

    /// Get the concentration data for the given time and z indices.
    /// The data is formatted for HTML display.
    /// @param timeIndex The time index
    /// @param zIndex The z index
    /// @return The concentration data for the given time and z indices
	public ConcentrationData getConcentrationData(int timeIndex, int zIndex) throws IOException, InvalidRangeException {

		// Find the variable
		Variable concentrationVar = ncFile.findVariable("concentration");
		if (concentrationVar == null) {
			System.out.println("Variable 'concentration' not found in NetCDF file.");
			return null;
		}

		try {
			// Get the shape of the concentration variable to determine y and x dimensions
			int[] shape = concentrationVar.getShape();
			int yDimSize = shape[2]; // y dimension size
			int xDimSize = shape[3]; // x dimension size

            double[][] values = new double[yDimSize][xDimSize];

			// We want to read all y and x values for the given time and z indices
			int[] origin = new int[]{timeIndex, zIndex, 0, 0};
			int[] shapeToRead = new int[]{1, 1, yDimSize, xDimSize};

			Array data = concentrationVar.read(origin, shapeToRead);
	
			for (int y = 0; y < yDimSize; y++) {
				for (int x = 0; x < xDimSize; x++) {
					double concentrationValue = data.getDouble(data.getIndex().set(0, 0, y, x));
                   values[y][x] = concentrationValue;
				}
			}

            return new ConcentrationData(timeIndex, zIndex, values);

		} catch (InvalidRangeException e) {
			System.out.println("Error reading data: Invalid range specified.");
		} catch (IOException e) {
			System.out.println("Error reading data: " + e.getMessage());
		}

		return null;
	}

	/// Get the detailed information of the NetCDF file.
	/// The information string is pre-formatted for HTML display.
	/// @return The detailed information of the NetCDF file
	public String getInfo() {
		return "<pre>" + ncFile.getDetailInfo() + "</pre>";
	}
};

/// Main class for the AerisProject application.
/// ./mvnw spring-boot:run to start the application.
/// The application will be available at http://localhost:8080
@SpringBootApplication
@RestController
public class AerisProjectApplication {

	static String filePath = "concentration.timeseries.nc";
	static NetCDF ncdFile = new NetCDF(filePath);

    /// Main method to start the Spring Boot application.
    /// @param args The command line arguments
    /// @throws Exception If an error occurs
    public static void main(String[] args) {
		SpringApplication.run(AerisProjectApplication.class, args);
	}

    public static String readHtmlFile() {
        String fileName = "html/index.html";
        String htmlContent = null;
        
        try {
            // Read all bytes from the file and convert them into a String
            htmlContent = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately in your application
        }
        
        return htmlContent;
    }

    /// Home page of the application.
	@GetMapping("/")
	public String home() {
        String ncdFileInfo = ncdFile.getInfo();
        String htmlContent = readHtmlFile();
        return ncdFileInfo + "\n" + htmlContent;
	}

    /// Endpoint to create or update data
    @GetMapping("/get-info")
    public String getInfo() {
        return ncdFile.getInfo();
    }

    /// Endpoint to create or update data
    @GetMapping("/get-data")
    public String getData(@RequestParam int timeIndex, @RequestParam int zIndex) {
		try {
			ConcentrationData data = ncdFile.getConcentrationData(timeIndex, zIndex);
            if (data == null) {
                return "Error getting data.";
            }
            return data.toJsonString();
		} catch (IOException | InvalidRangeException e) {
			return "Error getting data: " + e.getMessage();
		}
	}

    /// Endpoint to create image from the NetCDF data
    @GetMapping(value = "/get-image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage(@RequestParam int timeIndex, @RequestParam int zIndex) {
        try {
            ConcentrationData data = ncdFile.getConcentrationData(timeIndex, zIndex);
            if (data == null) {
                return null;
            }
            byte[] concentrationPng = data.generateImage();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(concentrationPng);
        } catch (IOException | InvalidRangeException e) {
            return null;
        }
    }
}
