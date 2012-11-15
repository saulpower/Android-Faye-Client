package com.moneydesktop.finance.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.moneydesktop.finance.ApplicationContext;

public class FileIO {

	public static List<String[]> loadCSV(int resource) throws IOException {

		List<String[]> csv = new ArrayList<String[]>();
		
		InputStream inputStream = ApplicationContext.getContext().getResources().openRawResource(resource);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		String line = null;

		while ((line = reader.readLine()) != null) {

			String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			csv.add(values);
		}
		
		return csv;
	}
}
