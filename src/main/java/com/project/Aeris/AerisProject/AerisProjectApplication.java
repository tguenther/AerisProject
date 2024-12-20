package com.project.Aeris.AerisProject;

import java.io.IOException;

import org.apache.catalina.valves.rewrite.Substitution.SubstitutionElement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableList;

import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.util.IO;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

class NetCDF {
	private NetcdfFile ncFile;

	public NetCDF(String filePath) {
		try {
			ncFile = ucar.nc2.NetcdfFiles.open(filePath);
		} catch (IOException e) {
			System.out.println("Error loading NetCDF file: " + e.getMessage());
		}
	}

	public void close() {
		try {
			ncFile.close();
		} catch (IOException e) {
			System.out.println("Error closing NetCDF file: " + e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	public ImmutableList<Dimension> getDimensions() {
		return ncFile.getDimensions();
	}

	public ImmutableList<Variable> getVariables() {
		return ncFile.getVariables();
	}

	public String getConcentrationData(int timeIndex, int zIndex) throws IOException, InvalidRangeException {

		String datastr = "";

		// Find the variable
		Variable concentrationVar = ncFile.findVariable("concentration");
		if (concentrationVar == null) {
			datastr = "Variable 'concentration' not found in NetCDF file.";
			// return null;
			return datastr;
		}

		try {

			// Get the shape of the concentration variable to determine y and x dimensions
			int[] shape = concentrationVar.getShape();
			int yDimSize = shape[2]; // y dimension size
			int xDimSize = shape[3]; // x dimension size

			// We want to read all y and x values for the given time and z indices
			int[] origin = new int[]{timeIndex, zIndex, 0, 0};
			int[] shapeToRead = new int[]{1, 1, yDimSize, xDimSize};

			Array data = concentrationVar.read(origin, shapeToRead);

			// Iterate over the data and print (assuming we want to see all values)
	
			for (int y = 0; y < yDimSize; y++) {
				for (int x = 0; x < xDimSize; x++) {
                    System.out.println("timeIndex: " + timeIndex + ", y: " + y + ", x: " + x);
					double concentrationValue = data.getDouble(data.getIndex().set(0, 0, y, x));
					datastr += String.format("Concentration at time=%d, z=%d, y=%d, x=%d: %.6f ug/m3%n\n", 
										timeIndex, zIndex, y, x, concentrationValue * 1e9);
				}
			}

		} catch (InvalidRangeException e) {
			// System.out.println("Error reading data: Invalid range specified.");
			datastr = "Error reading data: Invalid range specified.";
		} catch (IOException e) {
			// System.out.println("Error reading data: " + e.getMessage());
			datastr = "Error reading data: " + e.getMessage();
		}

		return "<pre>" + datastr + "</pre>";
	}

	/// Get the detailed information of the NetCDF file.
	/// The information string is pre-formatted for HTML display.
	/// @return The detailed information of the NetCDF file
	public String getInfo() {
		return "<pre>" + ncFile.getDetailInfo() + "</pre>";
	}
};

@SpringBootApplication
@RestController
public class AerisProjectApplication {

	static String filePath = "concentration.timeseries.nc";
	static NetCDF ncdFile = new NetCDF(filePath);

	public static void main(String[] args) {
		SpringApplication.run(AerisProjectApplication.class, args);
	}

	@GetMapping("/")
	@SuppressWarnings("deprecation")
	public String home() {
		String response = "NetCDF file loaded successfully.\n\n";
		response += "Dimensions:\n";
		for (Dimension dim : ncdFile.getDimensions()) {
			response += dim.getFullName() + ": " + dim.getLength() + "\n";
		}
		response += "\nVariables:\n";
		for (Variable var : ncdFile.getVariables()) {
			response += var.getFullName() + " - Shape: " + java.util.Arrays.toString(var.getShape()) + "\n";
		}

		return "<pre>" + response + "</pre>";
	}

    // Endpoint to create or update data
    @GetMapping("/get-info")
    public String getInfo() {
        return ncdFile.getInfo();
    }

    // Endpoint to create or update data
    @GetMapping("/get-data")
    public String getData(@RequestParam int timeIndex, @RequestParam int zIndex) {
		try {
			return ncdFile.getConcentrationData(timeIndex, zIndex);
			// return "Got data for time index " + timeIndex + " and z index " + zIndex;
		} catch (IOException | InvalidRangeException e) {
			return "Error getting data: " + e.getMessage();
		}
	}

    @GetMapping("/get-image")
    public String getImage(@RequestParam Long timeIndex, @RequestParam Long zIndex) {
        return "Should return Image for " + timeIndex + " and " + zIndex;
    }

	// Load NetCDF file
	@SuppressWarnings("deprecation")
	static private void loadNcdfFile(String filePath) {
        try(NetcdfFile ncFile = ucar.nc2.NetcdfFiles.open(filePath)) {
			System.out.println("NetCDF file loaded successfully.");
            System.out.println("Dimensions:");
            ncFile.getDimensions().forEach(dim -> {
                System.out.println(dim.getFullName() + ": " + dim.getLength());
            });

            // Print variables and their shapes (which correspond to dimension indices)
            System.out.println("\nVariables:");
            for (Variable var : ncFile.getVariables()) {
                System.out.println(var.getFullName() + " - Shape: " + java.util.Arrays.toString(var.getShape()));
                
                // Example of reading data for a variable
                if (var.getRank() > 0) { // Check if the variable has dimensions
                    int[] origin = new int[var.getRank()]; // Start at the first element of each dimension
                    int[] shape = var.getShape(); // Read all elements
                    try {
                        Array data = var.read(origin, shape);
                        // Here you would handle the data array, e.g., print it or process it
                        System.out.println("Data for variable " + var.getFullName() + " has been read.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
		} catch (Exception e) {
			System.out.println("Error loading NetCDF file: " + e.getMessage());
		}
	}
}
