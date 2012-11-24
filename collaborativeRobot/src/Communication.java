import java.util.Arrays;
import java.util.Comparator;


public class Communication {

	public static String[] dataToProcess = new String[5];
	public static int order = 0;
	
	public static void init() {
		for(int i=0;i<dataToProcess.length;i++)
			dataToProcess[i] = "";
	}
	
	/*
	 * order represents which probable point we want (ie most probable, second most probable)
	 * returns a string with the format: "x-y-value|value;value;value|value..."
	 * values from top-left to bottom-right from left to right | represents a
	 * row change, ; represents a column change in a normal case it would be
	 * 8(+1) values but we need to consider limit cases so they can be less than 8
	 */
	public static String getProbableBeacon(int order) {

		int maxValues[][] = new int[order][3];
		
		for (int i = 0; i < jClient.beaconProbability.length; i++)
			for (int j = 0; j < jClient.beaconProbability[i].length; j++) {
				int minimum = getMinimum(maxValues);
				if (jClient.beaconProbability[i][j] > (maxValues[minimum][0]/100.0)) {
					maxValues[minimum][1] = i;
					maxValues[minimum][0] = ((int)(jClient.beaconProbability[i][j] *100));
					maxValues[minimum][2] = j;
				}
			}
		
		Arrays.sort(maxValues, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return ((Integer) o2[0]).compareTo(o1[0]);
            }
        });

		int iMax = maxValues[order-1][1];
		int jMax = maxValues[order-1][2];
		
		String ret = iMax + "-" + jMax + "-"
				+ ((int) (jClient.beaconProbability[iMax][jMax] * 100)) + "|";

		for (int i = Math.max(iMax - 1, 0); i <= Math.min(iMax + 1,jClient.mapSizeY - 1); i++) {
			for (int j = Math.max(jMax - 1, 0); j <= Math.min(jMax+ 1, jClient.mapSizeX - 1); j++) {
				if (i == iMax && j == jMax)
					continue;
				ret += ((int) (jClient.beaconProbability[i][j] * 100)) + ";";
			}
			ret = ret.substring(0, ret.length() - 1);
			ret += "|";
		}

		return ret;
	}
	
	private static int getMinimum(int[][] maxValues) {
		int min = Integer.MAX_VALUE;
		int iMin = 0;
		for (int i=0;i<maxValues.length;i++)
			if (maxValues[i][0] < min) {
				min = maxValues[0][0];
				iMin = i;
			}
		return iMin;
	}
	
	public static void decodeAndApplyProbableBeaconMessage(String message) {

		String[] lines = message.split("\\|");

		String beaconMostProbablePoint[] = lines[0].split("-");
		
		int mostProbableY = Integer.parseInt(beaconMostProbablePoint[0]);
		int mostProbableX = Integer.parseInt(beaconMostProbablePoint[1]);
		double value = Integer.parseInt(beaconMostProbablePoint[2]) / 100.0;
		
		jClient.beaconProbability[mostProbableY][mostProbableX] = value;

		if (lines.length == 3) { // corner case

			// ignore? not worth the work, besides the beacon won't be at the
			// corner of the matrix...

		} else {
			
			String[] firstLine = lines[1].split(";");
			String[] secondLine = lines[2].split(";");
			String[] thirdLine = lines[3].split(";");

			// modify to use the update probability function 
			jClient.beaconProbability[mostProbableY - 1][mostProbableX - 1] = Integer.parseInt(firstLine[0]) / 100.0;
			jClient.beaconProbability[mostProbableY - 1][mostProbableX] = Integer.parseInt(firstLine[1]) / 100.0;
			jClient.beaconProbability[mostProbableY - 1][mostProbableX + 1] = Integer.parseInt(firstLine[2]) / 100.0;

			jClient.beaconProbability[mostProbableY][mostProbableX - 1] = Integer.parseInt(secondLine[0]) / 100.0;
			jClient.beaconProbability[mostProbableY][mostProbableX + 1] = Integer.parseInt(secondLine[1]) / 100.0;

			jClient.beaconProbability[mostProbableY + 1][mostProbableX - 1] = Integer.parseInt(thirdLine[0]) / 100.0;
			jClient.beaconProbability[mostProbableY + 1][mostProbableX] = Integer.parseInt(thirdLine[1]) / 100.0;
			jClient.beaconProbability[mostProbableY + 1][mostProbableX + 1] = Integer.parseInt(thirdLine[2]) / 100.0;
		}
	}

	public static void say() {
		String probableBeacon = Communication.getProbableBeacon((order++ % 5)+1);
		jClient.cif.Say(probableBeacon);
	}

	public static void receive() {
		for (int i = 1; i <= 5; i++) {
			if (i == jClient.pos || jClient.cif.GetMessageFrom(i) == null)
				continue;
			if (dataToProcess[i-1].equals(jClient.cif.GetMessageFrom(i)))
				continue;
			else {
				dataToProcess[i - 1] = jClient.cif.GetMessageFrom(i);
				Communication.decodeAndApplyProbableBeaconMessage(dataToProcess[i - 1]);
				System.out.println("message from " + i + ": " + dataToProcess[i - 1]);
			}
		}	
	}
}
