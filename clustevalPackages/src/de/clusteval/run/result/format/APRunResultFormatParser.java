/**
 * 
 */
package de.clusteval.run.result.format;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.text.TextFileMapParser;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class APRunResultFormatParser extends RunResultFormatParser {

	protected Map<String, String> idMapping;
	protected Map<Integer, Set<Integer>> cluster;

	/**
	 * @param internalParams
	 * @param params
	 * @param absFilePath
	 * @throws IOException
	 */
	// TODO: rename all RunResult* to ClusteringResult*
	public APRunResultFormatParser(final Map<String, String> internalParams,
			final Map<String, String> params, final String absFilePath)
			throws IOException {
		super(internalParams, params, FileUtils.buildPath(absFilePath,
				"idx.txt"), false, OUTPUT_MODE.BURST);
		this.countLines();
		this.cluster = new HashMap<Integer, Set<Integer>>();

		// read in ID mapping
		String input = null;
		for (String pa : internalParams.keySet())
			if (pa.equals("i")) {
				input = internalParams.get(pa);
				break;
			}

		TextFileMapParser mappingParser = new TextFileMapParser(input + ".map",
				1, 0);
		mappingParser.process();
		this.idMapping = mappingParser.getResult();
	}

	@Override
	protected void processLine(@SuppressWarnings("unused") String[] key,
			String[] value) {
		Integer cluster = Integer.valueOf(value[0]);
		Integer id = (int) this.currentLine + 1;
		if (!(this.cluster.containsKey(cluster)))
			this.cluster.put(cluster, new HashSet<Integer>());
		this.cluster.get(cluster).add(id);
	}

	@Override
	protected String getBurstOutput() {
		StringBuilder sb = new StringBuilder();

		String maxits = null;
		String convits = null;
		String dampfact = null;
		String preference = null;
		for (String pa : params.keySet())
			if (pa.equals("maxits")) {
				maxits = params.get(pa);
			} else if (pa.equals("convits")) {
				convits = params.get(pa);
			} else if (pa.equals("dampfact")) {
				dampfact = params.get(pa);
			} else if (pa.equals("preference")) {
				preference = params.get(pa);
			}

		/*
		 * Write header line
		 */
		sb.append("maxits");
		sb.append(",");
		sb.append("convits");
		sb.append(",");
		sb.append("dampfact");
		sb.append(",");
		sb.append("preference");
		sb.append("\t");
		sb.append("Clustering");
		sb.append(System.getProperty("line.separator"));

		sb.append(maxits);
		sb.append(",");
		sb.append(convits);
		sb.append(",");
		sb.append(dampfact);
		sb.append(",");
		sb.append(preference);
		sb.append("\t");

		for (Integer cluster : this.cluster.keySet()) {
			for (Integer id : this.cluster.get(cluster)) {
				String idMap = this.idMapping.get(id + "");
				sb.append(idMap);
				sb.append(":1.0,");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(";");
		}
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	@Override
	public void finishProcess() {
		super.finishProcess();
		// move input file
		String folderPath = new File(this.absoluteFilePath).getParentFile()
				.getParentFile().getAbsolutePath();
		String newFileName = new File(this.absoluteFilePath).getParentFile()
				.getName();
		String fileName1 = new File(this.absoluteFilePath).getName();
		new File(this.absoluteFilePath).renameTo(new File(folderPath
				+ System.getProperty("file.separator") + fileName1));

		// move converted output file
		folderPath = new File(this.outputFile).getParentFile().getParentFile()
				.getAbsolutePath();
		String fileName2 = new File(this.outputFile).getName();
		new File(this.outputFile).renameTo(new File(folderPath
				+ System.getProperty("file.separator") + fileName2));

		FileUtils.delete(new File(this.absoluteFilePath).getParentFile());

		new File(folderPath + System.getProperty("file.separator") + fileName1)
				.renameTo(new File(folderPath
						+ System.getProperty("file.separator") + newFileName));
		new File(folderPath + System.getProperty("file.separator") + fileName2)
				.renameTo(new File(folderPath
						+ System.getProperty("file.separator") + newFileName
						+ ".conv"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.format.RunResultFormatParser#convertToStandardFormat()
	 */
	@Override
	public void convertToStandardFormat() throws IOException {
		this.process();
	}
}