package edu.umassmed.microscopyMetadataReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.common.xml.XMLTools;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.services.OMEXMLService;
import loci.formats.services.OMEXMLServiceImpl;
import ome.codecs.MissingLibraryException;
import ome.xml.meta.MetadataStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MicroscopyMetadataReader {
	
	static boolean debug = false;
	static boolean printFiles = true;

	static String settingsMapFileName = "image_settings_map.txt";

	static int xmlSpaces = 3;
	
	public static Map<String, String> importSettingsMap(final String fileName)
			throws IOException {
		final Map<String, String> settingsMap = new LinkedHashMap<String, String>();

		final File file = new File(fileName);
		final FileReader isr = new FileReader(file);
		final BufferedReader br = new BufferedReader(isr);

		String line = br.readLine();
		while (line != null) {
			if (line.startsWith("//") || line.equals("")) {
				line = br.readLine();
				continue;
			}
			final String[] values = line.split(":");
			final String originalValue = values[0];
			final String newValue = values[1];
			
			settingsMap.put(originalValue, newValue);

			line = br.readLine();
		}

		isr.close();
		br.close();
		return settingsMap;
	}

	public static String getOMEXML(final IFormatReader reader)
			throws MissingLibraryException, ServiceException {
		final MetadataStore ms = reader.getMetadataStore();

		OMEXMLService service;
		try {
			final ServiceFactory factory = new ServiceFactory();
			service = factory.getInstance(OMEXMLService.class);
		} catch (final DependencyException de) {
			throw new MissingLibraryException(OMEXMLServiceImpl.NO_OME_XML_MSG,
					de);
		}
		service.getOMEXMLVersion(ms);
		if (ms instanceof MetadataRetrieve)
			return service.getOMEXML((MetadataRetrieve) ms);
		// System.out.println(XMLTools.indentXML(xml,
		// MicroscopyMetadataReader.xmlSpaces, true));
		else
			// System.out
			// .println("The metadata could not be converted to OME-XML.");
			return null;
	}

	public static String transformDate(final String attrDate) {
		final String dates[] = attrDate.split("T");
		final String date = dates[0];
		final String time = dates[1];
		final String dateSplit[] = date.split("-");
		final String dateYear = dateSplit[0];
		final String dateMonth = dateSplit[1];
		final String dateDay = dateSplit[2];
		final String newDate = dateDay + "/" + dateMonth + "/" + dateYear
				+ " @ " + time;
		return newDate;
	}

	public static Map<String, List<String>> mergeMap(
			final Map<String, List<String>> map1,
			final Map<String, List<String>> map2) {
		final Map<String, List<String>> mergedMap = new LinkedHashMap<String, List<String>>();
		mergedMap.putAll(map1);
		for (final String key2 : map2.keySet()) {
			final List<String> list2 = map2.get(key2);
			if (mergedMap.containsKey(key2)) {
				final List<String> list1 = mergedMap.get(key2);
				list2.addAll(list1);
			}
			mergedMap.put(key2, list2);

		}
		return mergedMap;
	}

	public static Map<String, List<String>> collectNodeFields(
			final String path, final Node node) {
		final Map<String, List<String>> valueMap = new LinkedHashMap<String, List<String>>();

		final NamedNodeMap attributes = node.getAttributes();
		if (attributes == null)
			return valueMap;
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node attrNode = attributes.item(i);
			final String attrName = attrNode.getNodeName();
			String newPath = null;
			if (path != null) {
				newPath = path + "." + attrName;
			} else {
				newPath = attrName;
			}
			String attrValue = null;
			attrValue = attrNode.getTextContent();
			final String s = attrNode.toString();
			if (attrValue == null) {
				attrValue = s.substring(s.indexOf("=") + 1);
			}
			if (attrName.toLowerCase().contains("date")) {
				attrValue = MicroscopyMetadataReader.transformDate(attrValue);
			}
			List<String> valueList = null;
			if (valueMap.containsKey(newPath)) {
				valueList = valueMap.get(newPath);
			} else {
				valueList = new ArrayList<String>();
			}
			valueList.add(attrValue);
			valueMap.put(newPath, valueList);
		}
		return valueMap;
	}

	public static Map<String, List<String>> collectNodeListFields(
			final String path, final NodeList childrenNodeList) {
		Map<String, List<String>> valueMap = new LinkedHashMap<String, List<String>>();
		final Map<String, Integer> arrayIndexes = new LinkedHashMap<String, Integer>();
		final List<String> found = new ArrayList<String>();
		final List<String> duplicates = new ArrayList<String>();
		for (int i = 0; i < childrenNodeList.getLength(); i++) {
			final Node node = childrenNodeList.item(i);
			final String nodeName = node.getNodeName();
			String newPath = null;
			if (path != null) {
				newPath = path + "." + nodeName;
			} else {
				newPath = nodeName;
			}

			if (found.contains(newPath)) {
				duplicates.add(newPath);
			} else {
				found.add(newPath);
			}
		}
		
		for (int i = 0; i < childrenNodeList.getLength(); i++) {
			// final String tag = oldTag + "TAG" + i + "|";
			final Node node = childrenNodeList.item(i);
			final String nodeName = node.getNodeName();
			String newPath = null;
			if (path != null) {
				newPath = path + "." + nodeName;
			} else {
				newPath = nodeName;
			}
			if (duplicates.contains(newPath)
					|| (newPath.contains("Channel") && !newPath
							.contains("Channel#"))
					|| (newPath.contains("Plane") && !newPath
							.contains("Plane#"))
					|| (newPath.contains("ImagingEnvironment") && !newPath
							.contains("ImagingEnvironment#"))) {
				Integer index = null;
				final String oldPath = newPath;
				if (arrayIndexes.containsKey(newPath)) {
					index = arrayIndexes.get(newPath);
				} else {
					index = 0;
				}
				newPath = newPath + "#" + index;
				index++;
				arrayIndexes.put(oldPath, index);
			}
			String nodeValue = node.getTextContent();
			if ((nodeValue != null) && !nodeValue.equals("")) {
				if (nodeName.toLowerCase().contains("date")) {
					nodeValue = MicroscopyMetadataReader
							.transformDate(nodeValue);
				}
				List<String> valueList = null;
				if (valueMap.containsKey(newPath)) {
					valueList = valueMap.get(newPath);
				} else {
					valueList = new ArrayList<String>();
				}
				valueList.add(nodeValue);
				valueMap.put(newPath, valueList);
			}
			final Map<String, List<String>> nodeValueMap = MicroscopyMetadataReader
					.collectNodeFields(newPath, node);
			valueMap = MicroscopyMetadataReader
					.mergeMap(valueMap, nodeValueMap);
			final NodeList subChildren = node.getChildNodes();
			if (subChildren.getLength() > 0) {
				final Map<String, List<String>> subValueMap = MicroscopyMetadataReader
						.collectNodeListFields(newPath, subChildren);
				valueMap = MicroscopyMetadataReader.mergeMap(valueMap,
						subValueMap);
			}
		}

		return valueMap;
	}

	public static Map<String, List<String>> collectAllFields(final String xml)
			throws ParserConfigurationException, SAXException, IOException {

		final InputSource is = new InputSource(new StringReader(xml));

		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// an instance of builder to parse the specified xml file
		final DocumentBuilder db = dbf.newDocumentBuilder();
		final Document doc = db.parse(is);
		doc.getDocumentElement().normalize();
		// System.out.println("Root element: "
		// + doc.getDocumentElement().getNodeName());

		final NodeList rootChildren = doc.getDocumentElement().getChildNodes();

		final Map<String, List<String>> valueMap = MicroscopyMetadataReader
				.collectNodeListFields(null, rootChildren);

		return valueMap;
	}
	
	public static List<List<String>> normalizeString(final String s) {
		final List<String> oldPartialKeys = new ArrayList<String>();
		final List<String> oldKeyNames = new ArrayList<String>();
		String normalized = s;
		
		while (normalized.contains("#")) {

			final String keySplit[] = normalized.split("#");
			final int i1 = keySplit[0].lastIndexOf(".") + 1;
			final int i2 = normalized.indexOf(".", i1 + 1);
			String oldPartialKey = null;
			if (i2 != -1) {
				oldPartialKey = normalized.substring(i1, i2);
			} else {
				oldPartialKey = normalized.substring(i1);
			}

			final String oldKeySplit[] = oldPartialKey.split("#");
			final String oldKeyName = oldKeySplit[0];
			normalized = normalized.replace(oldPartialKey, oldKeyName);

			oldPartialKeys.add(oldPartialKey);
			oldKeyNames.add(oldKeyName);
		}
		
		final List<String> normalizedList = new ArrayList<String>();
		normalizedList.add(normalized);
		
		final List<List<String>> values = new ArrayList<List<String>>();
		values.add(normalizedList);
		values.add(oldPartialKeys);
		values.add(oldKeyNames);
		return values;
	}

	public static String createJSONString(
			final Map<String, List<String>> valueMap,
			final Map<String, String> settingsMap) throws JSONException {

		final List<String> mainObjs = new ArrayList<String>();
		for (final String key : valueMap.keySet()) {
			String newKey = null;
			if (key.contains("#")) {
				final List<List<String>> normalizedValues = MicroscopyMetadataReader
						.normalizeString(key);
				final String normalized = normalizedValues.get(0).get(0);
				final List<String> oldPartialKeys = normalizedValues.get(1);
				final List<String> oldKeyNames = normalizedValues.get(2);
				final String unmodifiedNewKey = settingsMap.get(normalized);
				if (unmodifiedNewKey != null) {
					newKey = unmodifiedNewKey;
					for (int i = 0; i < oldPartialKeys.size(); i++) {
						final String keyToReplace = oldKeyNames.get(i);
						final String replacementKey = oldPartialKeys.get(i);
						newKey = newKey.replace(keyToReplace, replacementKey);
					}
				}
			} else {
				newKey = settingsMap.get(key);
			}
			if (newKey == null) {
				continue;
			}

			final String newKeySplit[] = newKey.split("\\.");
			final String attrKey = newKeySplit[newKeySplit.length - 1];
			final String newKeyNoAttr = newKey.replace("." + attrKey, "");
			boolean notfound = true;
			int replaceIndex = -1;
			for (final String mainKey : mainObjs) {
				if (newKeyNoAttr.contains(mainKey)
						&& newKeyNoAttr.replace(mainKey, "").contains(".")) {
					notfound = false;
					replaceIndex = mainObjs.indexOf(mainKey);
				} else if (mainKey.contains(newKeyNoAttr)
						&& mainKey.replace(newKeyNoAttr, "").contains(".")) {
					notfound = false;
				} else {

				}
			}
			if (notfound) {
				mainObjs.add(newKeyNoAttr);
			}
			if (replaceIndex != -1) {
				mainObjs.remove(replaceIndex);
				mainObjs.add(replaceIndex, newKeyNoAttr);
			}
		}

		final JSONObject obj = new JSONObject();
		for (final String objToCreate : mainObjs) {
			final String pathToCreate[] = objToCreate.split("\\.");
			JSONObject currentObj = obj;
			for (int i = 0; i < (pathToCreate.length); i++) {
				final String pToCreate = pathToCreate[i];
				if (pToCreate.contains("#")) {
					final String pToCreateSplit[] = pToCreate.split("#");
					final String arrayName = pToCreateSplit[0] + "s";
					final Integer index = Integer.valueOf(pToCreateSplit[1]);
					JSONObject newObj = null;
					JSONArray newArray = null;
					try {
						newArray = currentObj.getJSONArray(arrayName);
						try {
							newObj = newArray.getJSONObject(index);
						} catch (final JSONException ex) {
							newObj = new JSONObject();
							newArray.put(index, newObj);
						}
						currentObj = newObj;
					} catch (final JSONException ex) {
						newArray = new JSONArray();
						currentObj.put(arrayName, newArray);
						newObj = new JSONObject();
						newArray.put(index, newObj);
					}
					currentObj = newObj;
				} else {
					JSONObject newObj = null;
					try {
						newObj = currentObj.getJSONObject(pToCreate);
					} catch (final JSONException ex) {
						newObj = new JSONObject();
						currentObj.put(pToCreate, newObj);
					}
					currentObj = newObj;
				}

			}
		}
		// System.out.println(obj.toString());
		
		for (final String key : valueMap.keySet()) {
			if (key == null) {
				if (MicroscopyMetadataReader.debug) {
					System.out.println("key is null");
				}
				continue;
			}
			String newKey = null;
			final String value = valueMap.get(key).get(0);
			if (key.contains("#")) {
				final List<List<String>> normalizedValues = MicroscopyMetadataReader
						.normalizeString(key);
				final String normalized = normalizedValues.get(0).get(0);
				final List<String> oldPartialKeys = normalizedValues.get(1);
				final List<String> oldKeyNames = normalizedValues.get(2);
				final String unmodifiedNewKey = settingsMap.get(normalized);
				if (unmodifiedNewKey != null) {
					newKey = unmodifiedNewKey;
					for (int i = 0; i < oldPartialKeys.size(); i++) {
						final String keyToReplace = oldKeyNames.get(i);
						final String replacementKey = oldPartialKeys.get(i);
						newKey = newKey.replace(keyToReplace, replacementKey);
					}
				}
			} else {
				newKey = settingsMap.get(key);
			}
			if (newKey == null) {
				if (MicroscopyMetadataReader.debug) {
					System.out.println(key);
				}
				continue;
			}

			JSONObject currentObj = obj;
			final String path[] = newKey.split("\\.");
			for (int i = 0; i < (path.length); i++) {
				final String pToAttr = path[i];
				if (i == (path.length - 1)) {
					Object newValue = value;
					final Pattern pattern = Pattern.compile("true|false",
							Pattern.CASE_INSENSITIVE);
					final Matcher matcher = pattern.matcher(value);
					if (matcher.matches()) {
						// if (Boolean.parseBoolean(value)) {
						if (MicroscopyMetadataReader.debug) {
							System.out.println(newValue + " is boolean");
						}
						newValue = Boolean.valueOf(value);
					} else if (MicroscopyMetadataReader.isNumeric(value)) {
						if (MicroscopyMetadataReader.debug) {
							System.out.println(newValue + " is double");
						}
						newValue = Double.valueOf(value);
					} else {
						if (MicroscopyMetadataReader.debug) {
							System.out.println(newValue + " is string");
						}
					}
					
					currentObj.put(pToAttr, newValue);
					continue;
				}
				if (pToAttr.contains("#")) {
					final String pToAttrSplit[] = pToAttr.split("#");
					final String arrayName = pToAttrSplit[0] + "s";
					final Integer index = Integer.valueOf(pToAttrSplit[1]);
					JSONObject newObj = null;
					JSONArray newArray = null;
					try {
						newArray = currentObj.getJSONArray(arrayName);
						newObj = newArray.getJSONObject(index);
						currentObj = newObj;
					} catch (final JSONException ex) {
						System.out.println("ERROR finding " + pToAttr);
						break;
					}
				} else {
					JSONObject nextObj = null;
					try {
						nextObj = currentObj.getJSONObject(pToAttr);
						currentObj = nextObj;
					} catch (final JSONException ex) {
						System.out.println("ERROR finding " + pToAttr);
						break;
					}
				}
			}
		}

		// for (final String oldKey : valueMap.keySet()) {
		// String newKey = settingsMap.get(oldKey);
		// if (newKey == null) {
		// continue;
		// }
		// final List<String> values = valueMap.get(oldKey);
		// newKey = newKey.replace("Image.", "");
		//
		// for (final String value : values) {
		// MicroscopyMetadataReader.writeField(obj, newKey, value);
		// }
		// }
		// System.out.println(obj.toString());
		return obj.toString();
	}
	
	public static boolean isNumeric(final String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}

	public static void main(final String[] args) {
		// parse command line arguments
		try {
			String id = null;
			if (args.length < 1) {
				// System.out.println("ERROR");
				// System.exit(1);
				final String dir = "E:\\Dropbox\\Micro-Meta App publication\\Micro-Meta App_CO-AUTHORS MATERIAL";
				final String subDir1 = "Z_Nitschke\\Nitschke Mic Pub2";
				final String subDir2 = "RawData";
				final String filename = "GSD1A_p15_Mito633_Calcein5uM_2 for Pub2.czi";
				id = dir + File.separator + subDir1 + File.separator + subDir2
						+ File.separator + filename;
				// System.out.println(id);
			} else {
				id = args[0];
			}
			int series = args.length > 1 ? Integer.parseInt(args[1]) : 0;
			final PrintStream origOut = System.out;
			System.setOut(null);

			// create OME-XML metadata store
			final ServiceFactory factory = new ServiceFactory();
			final OMEXMLService service = factory
					.getInstance(OMEXMLService.class);
			final IMetadata meta = service.createOMEXMLMetadata();
			// create format reader
			final IFormatReader reader = new ImageReader();
			reader.setMetadataStore(meta);
			// initialize file
			reader.setId(id);

			final int seriesCount = reader.getSeriesCount();
			if (series < seriesCount) {
				reader.setSeries(series);
			}
			series = reader.getSeries();

			final File f = new File(System.getProperty("user.dir"));
			final File dir = f.getAbsoluteFile();
			String directoryPath = dir.toString();
			String settingsFileName = directoryPath + File.separator
					+ MicroscopyMetadataReader.settingsMapFileName;
			if (!new File(settingsFileName).isFile()) {
				final URL codeSourceLocation = MicroscopyMetadataReader.class
						.getProtectionDomain().getCodeSource().getLocation();
				directoryPath = new File(codeSourceLocation.getPath())
						.getParentFile().getAbsolutePath();
				settingsFileName = directoryPath + File.separator
						+ MicroscopyMetadataReader.settingsMapFileName;
			}

			final Map<String, String> settingsMap = MicroscopyMetadataReader
					.importSettingsMap(settingsFileName);

			final String xml = MicroscopyMetadataReader.getOMEXML(reader);

			final String indentXML = XMLTools.indentXML(xml,
					MicroscopyMetadataReader.xmlSpaces, true);
			if (MicroscopyMetadataReader.printFiles) {
				final String fileName = id + "-xml.txt";
				final FileWriter fw = new FileWriter(fileName);
				final BufferedWriter bw = new BufferedWriter(fw);
				bw.write(indentXML);
				bw.close();
				fw.close();
			}
			// System.out.println(settingsMap);

			final Map<String, List<String>> valueMap = MicroscopyMetadataReader
					.collectAllFields(xml);
			// fileName = id + ".json";
			// fw = new FileWriter(fileName);
			// bw = new BufferedWriter(fw);
			// bw.write(jsonString);
			// bw.close();
			// fw.close();
			// for (final String s : valueMap.keySet()) {
			// if (s.contains("TiffData")) {
			// continue;
			// }
			// System.out.println(s + " - " + valueMap.get(s));
			// }

			if (MicroscopyMetadataReader.debug) {
				System.setOut(origOut);
			}

			final String jsonString = MicroscopyMetadataReader
					.createJSONString(valueMap, settingsMap);
			if (MicroscopyMetadataReader.printFiles) {
				final String fileName = id + ".json";
				final FileWriter fw = new FileWriter(fileName);
				final BufferedWriter bw = new BufferedWriter(fw);
				bw.write(jsonString);
				bw.close();
				fw.close();
			}
			System.setOut(origOut);
			System.out.println(jsonString);
		} catch (final Exception ex) {
			if (MicroscopyMetadataReader.debug) {
				ex.printStackTrace();
			} else {
				System.out.println("ERROR");
			}
		}
	}
}
