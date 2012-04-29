/**
 * Text Indexer and Feature Extractor
 */
package ir;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.tartarus.snowball.SnowballStemmer;
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;
import java.lang.Math;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Andreas Decker e0227283@student.tuwien.ac.at
 * 
 */
public class MyTIandFE {
	private String input_dir = "C:\\IR\\20_newsgroups_small";
	private String output_file = "C:\\IR\\myARFF.arff";
	private String weighting = "BOOL";
	private double upper_thr = 2;
	private double lower_thr = 0;
	private boolean stemming = true;
	private String stemming_lang = "english";
	private int stemming_count = 1;
	private boolean stopwords = true;
	private String stopword_file = ".\\ENG_Stopwords.txt";
	private String temp_dir = "C:\\tmp\\";

	public interface IndexerOptions
	{
	  @Option(shortName="i") String getInput_dir();
	  @Option(shortName="o", defaultToNull=true) String getOutput_file();
	  @Option(shortName="w", defaultToNull=true) String getWeighting();	  
	  @Option(shortName="u", defaultToNull=true) String getUpper_thr();
	  @Option(shortName="l", defaultToNull=true) String getLower_thr();
	  @Option(shortName="ste", defaultToNull=true) String getStemming();
	  @Option(shortName="stelang", defaultToNull=true) String getStemming_lang();
	  @Option(shortName="stecount", defaultToNull=true) String getStemming_count();	  
	  @Option(shortName="sto", defaultToNull=true) String getStopwords();
	  @Option(shortName="stofil", defaultToNull=true) String getStopword_file();
	  @Option(shortName="tmp", defaultToNull=true) String getTemp_dir();
	}
	
	private LinkedHashMap<String, Integer> dictionary = new LinkedHashMap<String, Integer>();
	private List<File> temp_doc_list = new ArrayList<File>();
	private int inputdoccount = 0;
	
	
	/**
	 * @param directoryPath
	 * @return
	 * @throws Exception
	 */
	public void createIndex() throws Exception {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output_file)));
        bw.write("@relation 'text_files_in_"+input_dir+"'");
        bw.newLine();
        bw.newLine();
        bw.write("@attribute 'filename' STRING");
        bw.newLine();
        bw.write("@attribute 'fileclass' STRING");        
        bw.newLine();

        
    	LinkedHashMap<String, Integer> reduced_dictionary = new LinkedHashMap<String, Integer>();
        
    	int total_document_count = inputdoccount;
		Iterator<String> dic_iterator = dictionary.keySet().iterator();      
        for (int p=2; dic_iterator.hasNext();){
        	String dict_entry = dic_iterator.next();
        	int doc_frequ = dictionary.get(dict_entry);
        	double boundry = (double)doc_frequ / (double) total_document_count;
        	if (lower_thr < boundry && boundry < upper_thr) {
        		reduced_dictionary.put(dict_entry, p);
        		p++;
        		bw.write("@attribute '"+dict_entry+ "' numeric");        
        		bw.newLine();
        	}
        }		
        bw.newLine();
        bw.write("@data");        
		
		
		// loop over all found files
		//System.out.print("Working on Document"); 
		for (int i = 0; i < temp_doc_list.size(); i++) {
			//System.out.println("READ File: " + temp_doc_list.get(i).toString());
			 //System.out.print("..."+i);
			
			try {
/*				String[] fullname = FilenameUtils.getName(temp_doc_list.get(i).toString()).split("_");

		        bw.newLine();
				bw.write("{0 "+fullname[1]+", 1 "+fullname[0]);  */      
				System.out.println("READ TEMP File: " + temp_doc_list.get(i).toString());			
				
				LinkedHashMap<String, LinkedHashMap<String, Integer>> collection = readHashfromDisk(temp_doc_list.get(i).toString());
				Iterator<String> coll_iterator = collection.keySet().iterator();      
				while (coll_iterator.hasNext()){
			       	String coll_entry = coll_iterator.next();
			       	LinkedHashMap<String, Integer> tokens = collection.get(coll_entry);					
			       	
			       	String[] fullname = coll_entry.split("_");
			        bw.newLine();
					bw.write("{0 "+fullname[1]+", 1 "+fullname[0]); 
				
				//LinkedHashMap<String, Integer> tokens = readHashfromDisk(temp_dir+fullname[0]+"_"+fullname[1]);
				if (tokens != null) { 
				
					Iterator<String> reddic_iterator = reduced_dictionary.keySet().iterator();      
					while (reddic_iterator.hasNext()){
						String akt_dict = reddic_iterator.next();
						if (tokens.containsKey(akt_dict)) {
							String line = ", "+reduced_dictionary.get(akt_dict)+" ";
							
							if (weighting.equals("BOOL"))
								line = line+"1";
							else if(weighting.equals("TF")) {
								//System.out.println("TF");
								Double tf = Math.log((double)1 + (double)tokens.get(akt_dict));
								BigDecimal bd = new BigDecimal(tf).setScale(4, RoundingMode.HALF_EVEN);
								line = line+bd.toString();
							}
							else if (weighting.equals("TFIDF")){
								//System.out.println("TFIDF");								
								Double tf  = Math.log((double)1 + (double)tokens.get(akt_dict));
								int doc_frequ = dictionary.get(akt_dict);
								Double idf = Math.log10((double)total_document_count / (double)doc_frequ);
								Double tfidf = tf*idf;
								BigDecimal bd = new BigDecimal(tfidf).setScale(4, RoundingMode.HALF_EVEN);								
								line = line+bd.toString();
							}
							else throw new Exception();
							
							bw.write(line); 								
						}
					}		
				}
				else System.out.println("No data for File: "+fullname[0]+"_"+fullname[1]);
				bw.write("}");  
				}
				

			} catch (Exception e) {/* TODO Auto-generated catch block */e.printStackTrace(); bw.close(); return;}
			  catch (Throwable e) {/* TODO Auto-generated catch block */e.printStackTrace(); bw.close(); return;}
		}

        bw.close();
		
	}
	
	public void extractFeatures() throws Exception {

		// Add temporary directory for extracted documents
		FileUtils.forceMkdir(new File(temp_dir));
		
		// Get all files from working directory and all it's subdir's
		List<File> list = new ArrayList<File>();
		getFiles(new File(input_dir), list);
		System.out.println(list.size()+" Documents found!");

		//List<LinkedHashMap<String, Integer>> collection = new LinkedList<LinkedHashMap<String, Integer>>();
		LinkedHashMap<String, LinkedHashMap<String, Integer>> collection = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
		
		// Loop over all found files
		inputdoccount = list.size();
		int tmpcount = 0;
		for (int i = 0; i < list.size(); i++) {
			//System.out.println("WRITE File: " + list.get(i).toString());

			// Set filename and it's containing directory name
			String path = FilenameUtils.getPathNoEndSeparator(list.get(i).toString());
			String dirname = path.substring(FilenameUtils.indexOfLastSeparator(path)+1);
			String filename = FilenameUtils.removeExtension(FilenameUtils.getName(list.get(i).toString()));

			// Read file from disk
			String ready = FileUtils.readFileToString(list.get(i), "UTF-8");
			
			// extract tokens from text + add to dictionary
			LinkedHashMap<String, Integer> tokens = scanForTokens(ready.toLowerCase());
			
			// Write extracted tokens into temp files
			collection.put(dirname+"_"+filename, tokens);
			if (tmpcount == 10000) {
				if (!writeHashtoDisk(collection, "tmp"+i+".tmp")) throw new Exception();
				temp_doc_list.add(new File("tmp"+i+".tmp"));				
				System.out.println("WRITE TEMP File: " + "tmp"+i+".tmp");			
				tmpcount = 1;
				collection.clear();
			}
			else tmpcount ++;
		}
		
		if (tmpcount != 1) {
			if (!writeHashtoDisk(collection, "tmpLast.tmp")) throw new Exception();
			temp_doc_list.add(new File("tmpLast.tmp"));	
			System.out.println("WRITE TEMP File: " + "tmpLast.tmp");			
			tmpcount = 0;
			collection.clear();			
		}
		
	}
	
	
	public boolean writeHashtoDisk(LinkedHashMap<String, LinkedHashMap<String, Integer>> savethisHash, String filename) {
	    boolean op_state = false;
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(savethisHash);
			oos.close();
			fos.close();
			op_state = true;
		} catch (Exception e) {
			System.out.println("Fehler beim Erstellen der temp Datei: "+filename);
			System.out.println("Abbruch!");
			op_state = false;
		}
		return op_state;
	}

	public LinkedHashMap<String, LinkedHashMap<String, Integer>> readHashfromDisk(String filename) {
		LinkedHashMap<String, LinkedHashMap<String, Integer>> inHashMap;
		try {
			FileInputStream fis = new FileInputStream(filename);
		    ObjectInputStream ois = new ObjectInputStream(fis);
		    inHashMap = (LinkedHashMap<String, LinkedHashMap<String, Integer>>) ois.readObject();
		    ois.close();	
		    fis.close();
		} catch (Exception e) {
			System.out.println("Fehler beim Lesen der temp Datei: "+filename);
			System.out.println("Abbruch!");
			inHashMap = null;
		}
		return inHashMap;
	}
	
	
	
	/**
	 * @param input
	 * @return
	 */
	public LinkedHashMap<String, Integer> scanForTokens(String input) {
		LinkedHashMap<String, Integer> tokens = new LinkedHashMap<String, Integer>();
        
        // Load stopwords from file
		Pattern stopwordlist = loadStopWords(stopword_file);
		
		// Scan text for tokens
        Scanner tokenizer = new Scanner(input);
        //tokenizer.useDelimiter("(\\s|,|;|!|>|<|=)"); // this means whitespace or comma usw.
        //tokenizer.useDelimiter("[\\s,;!><=\\(\\)\\\\\\+/\\\"]");
        //tokenizer.useDelimiter(Pattern.compile("[\\s<>%\\$?\\(\\),;=/!\"'\\^§%&\\[\\]\\{\\}\\+\\-´_]"));
        tokenizer.useDelimiter(Pattern.compile("[^0-9a-zA-Z.@':]+"));
        
        while (tokenizer.hasNext()) {			   // check each token
        	String token = tokenizer.next();
        	token = token.replaceAll("'", "");
        	//token.replaceAll("[^0-9a-zA-Z.//@]+","");
        	if (stopwords) token = stopwordlist.matcher(token).replaceAll("");  // remove stopwords        	
        	token = trimToken(token);                                           // trim token on beginning and at end
        	if (stemming) token = stemToken(token);								// stem token
        	/*token = token.replaceAll("\\(", "");
        	token = token.replaceAll("\\)", "");        	
        	token = token.replaceAll("\"", "");   */     	        	
			if (!token.equals("")) {
				// Add to document token list
				int doc_anz = 1;
				if (tokens.containsKey(token)) doc_anz = tokens.get(token) +1;
				tokens.put(token, doc_anz);      
				
				// Add to dictionary token list
				if (doc_anz == 1) {
					int dict_anz = 1;
					if (dictionary.containsKey(token)) dict_anz = dictionary.get(token) +1;
					dictionary.put(token, dict_anz);      
				}
			}
        }
		return tokens;		
	}
	
	/**
	 * @param token
	 * @return
	 */
	public String stemToken(String token) {
		@SuppressWarnings("rawtypes")
		Class stemClass;
		try {
			stemClass = Class.forName("org.tartarus.snowball.ext." + stemming_lang + "Stemmer");
	        SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
			if (token.length() > 0) {
			    stemmer.setCurrent(token);
			    for (int i = stemming_count; i != 0; i--) 
			    	stemmer.stem();
			    token = (stemmer.getCurrent());
			}			
		} catch (Exception e) {
			System.out.println("Cannot use stemmer for language: "+stemming_lang);
			System.out.println("Skip stemming!");			
		}		
		return token;
	}
	
	/**
	 * @param token
	 * @return
	 */
	public String trimToken(String token) {
		Pattern p = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);  // Filter special characters
		if (token.length() <= 0 || token == null) return "";
		while (p.matcher(token.substring(token.length()-1)).find()) {        // Filter at end of token
			if (token.length() == 1) return "";
			else token = token.substring(0, token.length()-1);
		}			
		while (p.matcher(token.substring(0, 1)).find()) {                    // Filter at begin of token
			if (token.length() == 1) return "";
			else token = token.substring(1, token.length());
		}					
		return token;
	}	
	

	/**
	 * @param stopwordfile
	 * @return
	 */
	public Pattern loadStopWords(String stopwordfile) {
		Pattern stopRegex = null;
		try {
			List<String> stopwords = FileUtils.readLines(new File(stopwordfile), "UTF-8");
			String words = null;
			for (int i=0; i < stopwords.size(); i++)
				words = words + "|" + stopwords.get(i);
			if (words.length() > 1) {
				words = words.substring(1);
				stopRegex = Pattern.compile("\\b(?:"+words+")\\b\\s*", Pattern.CASE_INSENSITIVE);
			}
		} catch (IOException e) { System.out.println("Error while reading stopwords! Stopwords are disabled!"); }
		return stopRegex;
	}
	
	
/*
	public String filterStopWords(String textline, Pattern stopRegex) {
		Matcher matcher = stopRegex.matcher(textline).re;
		return matcher.replaceAll("");
	}
*/
	/**
	 * @param folder
	 * @param list
	 */
	private static void getFiles(File folder, List<File> list) {
		folder.setReadOnly();
		File[] files = folder.listFiles();
		for (int j = 0; j < files.length; j++) {
			// list.add(files[j]);
			if (files[j].isDirectory())
				getFiles(files[j], list);
			else
				list.add(files[j]);
		}
	}

	/**
	 * @param zip_file_name
	 * @param file_name
	 * @param dataset
	 * @return
	 * @throws Exception
	 */
/*	public boolean writeToZipFile(String zip_file_name, String file_name,
			Instances dataset) throws Exception {
		ZipOutputStream zos = null;

		zos = new ZipOutputStream(new FileOutputStream(zip_file_name));
		zos.setLevel(Deflater.BEST_COMPRESSION);
		zos.putNextEntry(new ZipEntry(file_name));
		zos.write(dataset.toString().getBytes());

		zos.closeEntry();
		zos.flush();
		if (zos != null)
			zos.close();

		return true;
	}*/

	/**
	 * @param args
	 * @return
	 */
	public boolean init(String[] args) {
		/* Programm.jar -i <input-dir> 
		 *              -o <output-dir> 
		 *              -w <BOOL|TF|TFIDF|ALL> 
		 *              -u <upperThreshold>
		 *              -l <lowerThreshold>
		 *              -noste <ON|OFF> 
		 *              -stelang <ENGLISH|GERMAN>
		 *              -stecount <int>
		 *              -nosto <ON|OFF>
		 *              -stofil <stopfile>
		 *              -tmp <tem-dir> */
			
		IndexerOptions result;
		try {
			result = CliFactory.parseArguments(IndexerOptions.class, args);
		} catch (ArgumentValidationException e) {return false;}
		
		if(result.getInput_dir()!=null) input_dir = result.getInput_dir();
		if(result.getOutput_file()!=null) output_file = result.getOutput_file();
		if (result.getWeighting()!=null) if(result.getWeighting().equals("BOOL")||result.getWeighting().equals("TF")||result.getWeighting().equals("TFIDF")||result.getWeighting().equals("ALL")) weighting = result.getWeighting();
		if (result.getUpper_thr()!=null){
			try {
				Double help_upper = Double.parseDouble(result.getUpper_thr());
				upper_thr = help_upper;
			} catch (NumberFormatException nfe) {}
		}
		if (result.getLower_thr()!=null){		
			try {
				Double help_lower = Double.parseDouble(result.getLower_thr());
				upper_thr = help_lower;
			} catch (NumberFormatException nfe) {}
		}
		if (result.getStemming()!=null) if (result.getStemming().equals("OFF")) stemming = false;
		if (result.getStemming_lang()!=null)if(result.getStemming_lang().equals("ENGLISH")||result.getStemming_lang().equals("DEUTSCH")) stemming_lang = result.getStemming_lang();
		if (result.getStemming_count()!=null){				
			try {
				Integer help_count = Integer.parseInt(result.getStemming_count());
				stemming_count = help_count;
			} catch (NumberFormatException nfe) {}
		}
		if (result.getStopwords()!=null) if (result.getStopwords().equals("OFF")) stopwords = false;
		if(result.getStopword_file()!=null) stopword_file = result.getStopword_file();
		if(result.getTemp_dir()!=null) temp_dir = result.getTemp_dir();
		
		return true;
	}
	
	/**
	 * @param startTime
	 * @return
	 */
	public static String getDuration(long startTime) {
		long duration = System.currentTimeMillis()-startTime;
		String outtime = null;
		
		if (TimeUnit.MILLISECONDS.toSeconds(duration) < 1)
			outtime = duration+" ms";
		else {
		outtime = String.format("%d min, %d sec", 
			    TimeUnit.MILLISECONDS.toMinutes(duration),
			    TimeUnit.MILLISECONDS.toSeconds(duration) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
			);
		}
		return ("Duration: "+ outtime);		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(">> BEGIN");
		long startTime = System.currentTimeMillis();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date start_date = new Date();
		System.out.println("-- "+dateFormat.format(start_date));	


		MyTIandFE tdta = new MyTIandFE();

		if (tdta.init(args)) {
			try {
				
				System.out.println("Start Feature Extractor....");
				tdta.extractFeatures();				
				System.out.println(getDuration(startTime));
				
				
				System.out.println("Start Indexer "+tdta.weighting+" ....");				
				if (tdta.weighting.equals("ALL")){
					String fpath = FilenameUtils.getFullPath(tdta.output_file);
					String fname = FilenameUtils.getBaseName(tdta.output_file);
					String fpfx = FilenameUtils.getExtension(tdta.output_file);
					
					tdta.weighting = "BOOL";
					tdta.output_file = fpath+fname+"_BOOL."+fpfx;
					System.out.println("Start Indexer "+tdta.weighting+" ....");
					tdta.createIndex();
					System.out.println(getDuration(startTime));				
					
					tdta.weighting = "TF";
					tdta.output_file = fpath+fname+"_TF."+fpfx;
					System.out.println("Start Indexer "+tdta.weighting+" ....");
					tdta.createIndex();
					System.out.println(getDuration(startTime));		
					
					tdta.weighting = "TFIDF";
					tdta.output_file = fpath+fname+"_TFIDF."+fpfx;
					System.out.println("Start Indexer "+tdta.weighting+" ....");					
					tdta.createIndex();					
				}
				else tdta.createIndex();
				
				// Remove temporary directory for extracted documents	
				FileUtils.deleteDirectory(new File(tdta.temp_dir));
				
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		} else
			System.out.println("Programm.jar -i <input-dir> -o <output-dir> -w <BOOL|TF|TFIDF|ALL> -u <upperThreshold> -l <lowerThreshold> -ste <ON|OFF> -stelang <ENGLISH|GERMAN> -stecount <int> -sto <ON|OFF> -stofil <stopfile> -tmp <tem-dir>");

		System.out.println(getDuration(startTime));
		Date end_date = new Date();
		System.out.println("-- "+dateFormat.format(end_date));			
		System.out.println("<< END");
	}

}