package luceneproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class InvertedIndexing {

	private static String Indexdir = null;
	private static final String fieldname[] = { "text_en", "text_es", "text_fr" };
	private static String inputFile = null;
	private static String outputFile = null;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		HashMap<String, LinkedList<Integer>> inverted_index = new HashMap<String, LinkedList<Integer>>();
		LinkedList<Integer> docs = new LinkedList<Integer>();
		LinkedList<Integer> temp = new LinkedList<Integer>();
		
		Indexdir = args[0];
		inputFile = args[2];
		outputFile = args[1];

		IndexReader reader = createReader();

		for (String field : fieldname) {

			Terms terms = getTerms(reader, field);

			TermsEnum termsenum = terms.iterator();

			createInvertedIndex(reader, termsenum, field, inverted_index, docs, temp);

		}

		FileInputStream fr_input = new FileInputStream(inputFile);
		BufferedReader br_input = new BufferedReader(new InputStreamReader(fr_input, "UTF-8"));

		readInputFile(br_input, inverted_index);
		
		fr_input.close();
		br_input.close();

	}

	private static void createInvertedIndex(IndexReader reader, TermsEnum termsenum, 
			String field, HashMap<String, LinkedList<Integer>> inverted_index, 
			LinkedList<Integer> docs, LinkedList<Integer> temp)
			throws Exception {
		// TODO Auto-generated method stub

		Integer docid;
		BytesRef term = termsenum.next();
		while (term != null) {

			docs = new LinkedList<Integer>();

			PostingsEnum penum = MultiFields.getTermDocsEnum(reader, field, term);
			while ((docid = penum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
				docs.add(docid);
			}

			String word = term.utf8ToString();

			if (inverted_index.containsKey(word)) {
				temp = inverted_index.get(word);
				temp = mergeTwoLists(docs, temp);
				inverted_index.put(word, temp);
			} else {
				inverted_index.put(word, docs);
			}
			term = termsenum.next();
		}
	}

	private static void readInputFile(BufferedReader br_input, HashMap<String, 
			LinkedList<Integer>> inverted_index) throws IOException {
		// TODO Auto-generated method stub
		
		File f_output = new File(outputFile);
		if(!f_output.exists()) {
			f_output.createNewFile();
		}
		FileWriter fr_output = new FileWriter(f_output.getAbsolutePath(), true);
		BufferedWriter br_output = new BufferedWriter(fr_output);
		
		String line;
		String[] line_term = null;
		while((line = br_input.readLine()) != null) {
			
			line_term = line.trim().split("\\s+");
			
			getPostings(line_term, br_output, inverted_index);
			
			TaatAnd(line_term, br_output, inverted_index);
			
			TaatOr(line_term, br_output, inverted_index);
			
			DaatAnd(line_term, br_output, inverted_index);
			
			DaatOr(line_term, br_output, inverted_index);
			
		}
		br_output.close();
		fr_output.close();
	}

	private static void TaatAnd(String[] line_term, BufferedWriter br_output,
			HashMap<String, LinkedList<Integer>> inverted_index) throws IOException {
		// TODO Auto-generated method stub
		
		br_output.write("TaatAnd");
		br_output.newLine();
		br_output.write(line_term[0]);
		br_output.write(" ");
		
		LinkedList<Integer> TaatAnd_list1 = new LinkedList<Integer>();
		LinkedList<Integer> TaatAnd_list2 = new LinkedList<Integer>();
		LinkedList<Integer> TaatAnd_final = new LinkedList<Integer>();
		if(inverted_index.containsKey(line_term[0]))
			TaatAnd_list1 = inverted_index.get(line_term[0]);
		
		int compare = 0;
		ListIterator<Integer> list1_iter = null;
		ListIterator<Integer> list2_iter = null;
		myIterator myIter_list1 = null;
		myIterator myIter_list2 = null;
		
		for(int index=1; index<line_term.length; index++) {
			
			br_output.write(line_term[index]);
			br_output.write(" ");
			if(TaatAnd_list1.isEmpty()) {
				break;
			}
			if(inverted_index.containsKey(line_term[index]))
				TaatAnd_list2 = inverted_index.get(line_term[index]);
			list1_iter = TaatAnd_list1.listIterator();
			list2_iter = TaatAnd_list2.listIterator();
			myIter_list1 = new myIterator(TaatAnd_list1);
			myIter_list2 = new myIterator(TaatAnd_list2);
			int jump_list1 = myIter_list1.getJump();
			int jump_list2 = myIter_list2.getJump();
			Integer temp = null;
			int k;
			Integer term1 = (Integer)list1_iter.next();
			Integer term2 = (Integer)list2_iter.next();

			while((list1_iter.hasNext() && (list2_iter.hasNext()))){
				
				if(term1 > term2) {
					if((TaatAnd_list2.indexOf(term2) % jump_list2) == 0) {
						if(myIter_list2.hasNext()) {
							temp = myIter_list2.next();
							if(term1 > temp) {
								k = myIter_list2.getIndex();
								list2_iter = TaatAnd_list2.listIterator(k);
								term2 = (Integer)list2_iter.next();
								compare++;
							}else {
								term2 = (Integer)list2_iter.next();
								compare++;
							}
						}else {
							term2 = (Integer)list2_iter.next();
						}
					}else {
						term2 = (Integer)list2_iter.next();
					}
					compare++;
				}else if(term1 < term2) {
					if((TaatAnd_list1.indexOf(term1) % jump_list1) == 0) {
						if(myIter_list1.hasNext()) {
							temp = myIter_list1.next();
							if(term2 > temp) {
								k = myIter_list1.getIndex();
								list1_iter = TaatAnd_list1.listIterator(k);
								term1 = (Integer)list1_iter.next();
								compare++;
							}else {
								term1 = (Integer)list1_iter.next();
								compare++;
							}
						}else {
							term1 = (Integer)list1_iter.next();
						}
					}else {
						term1 = (Integer)list1_iter.next();
					}
					compare++;
				}else {
					TaatAnd_final.add(term1);
					term1 = (Integer)list1_iter.next();
					term2 = (Integer)list2_iter.next();
					compare++;
				}
			}
			
			int br = 0;
			if(list1_iter.hasNext()) {
				while(list1_iter.hasNext()) {
					if(term1 < term2) {
						term1 = list1_iter.next();
						compare++;
					}else if(term1 > term2) {
						br = 1;
						compare++;
						break;
					}else {
						TaatAnd_final.add(term1);
						term1 = list1_iter.next();
						compare++;
					}
				}
				if(br == 0) {
					if(term1 == term2) {
						TaatAnd_final.add(term1);
						compare++;
					}
				}	
			}else if(list2_iter.hasNext()) {
				while(list2_iter.hasNext()) {
					if(term1 > term2) {
						term2 = list2_iter.next();
						compare++;
					}else if(term1 < term2) {
						br = 1;
						compare++;
						break;
					}else {
						TaatAnd_final.add(term1);
						term2 = list2_iter.next();
						compare++;
					}
				}
				if(br == 0) {
					if(term1 == term2) {
						TaatAnd_final.add(term1);
						compare++;
					}
				}
			}
			TaatAnd_list1 = TaatAnd_final;
			TaatAnd_final = new LinkedList<Integer>();
		}
		
		br_output.newLine();
		br_output.write("Results: ");
		if(TaatAnd_list1 == null) {
			br_output.write("empty");
		}else {
			if(TaatAnd_list1.isEmpty()) {
				br_output.write("empty");
			}else {
				for(Integer doc : TaatAnd_list1) {
					br_output.write(doc.toString());
					br_output.write(" ");
				}
			}
		}
		br_output.newLine();
		br_output.write("Number of documents in results: " + TaatAnd_list1.size());
		br_output.newLine();
		br_output.write("Number of comparisons: " + compare);
		br_output.newLine();
	}

	private static void TaatOr(String[] line_term, BufferedWriter br_output,
			HashMap<String, LinkedList<Integer>> inverted_index) throws IOException {
		// TODO Auto-generated method stub
		
		br_output.write("TaatOr");
		br_output.newLine();
		br_output.write(line_term[0]);
		br_output.write(" ");
		
		LinkedList<Integer> TaatOr_list1 = new LinkedList<Integer>();
		LinkedList<Integer> TaatOr_list2 = new LinkedList<Integer>();
		LinkedList<Integer> TaatOr_final = new LinkedList<Integer>();
		if(inverted_index.containsKey(line_term[0]))
			TaatOr_list1 = inverted_index.get(line_term[0]);
		int index_list1 = 0, index_list2 = 0, compare = 0;
		for(int index=1; index<line_term.length; index++) {
			index_list1 = 0;
			index_list2 = 0;
			br_output.write(line_term[index]);
			br_output.write(" ");
			
			if(inverted_index.containsKey(line_term[index]))
				TaatOr_list2 = inverted_index.get(line_term[index]);
			while((index_list1 < TaatOr_list1.size()) && (index_list2 < TaatOr_list2.size())) {
				if(TaatOr_list2.get(index_list2) > TaatOr_list1.get(index_list1)){
					TaatOr_final.add(TaatOr_list1.get(index_list1));
					index_list1++;
					compare++;
				}else if(TaatOr_list2.get(index_list2) < TaatOr_list1.get(index_list1)) {
					TaatOr_final.add(TaatOr_list2.get(index_list2));
					index_list2++;
					compare++;
				}else {
					TaatOr_final.add(TaatOr_list2.get(index_list2));
					index_list1++;
					index_list2++;
					compare++;
				}
			}
			
			
			if(index_list1 < TaatOr_list1.size()) {
				while(index_list1 < TaatOr_list1.size()) {
					TaatOr_final.add(TaatOr_list1.get(index_list1));
					index_list1++;
				}
			}
			
			if(index_list2 < TaatOr_list2.size()) {
				while(index_list2 < TaatOr_list2.size()) {
					TaatOr_final.add(TaatOr_list2.get(index_list2));
					index_list2++;
				}
			}
			
			TaatOr_list1 = TaatOr_final;
			TaatOr_final = new LinkedList<Integer>();
		}
		
		br_output.newLine();
		br_output.write("Results: ");
		if(TaatOr_list1 == null) {
			br_output.write("empty");
		}else {
			if(TaatOr_list1.isEmpty()) {
				br_output.write("empty");
			}else {
				for(Integer doc : TaatOr_list1) {
					br_output.write(doc.toString());
					br_output.write(" ");
				}
			}
		}
		br_output.newLine();
		br_output.write("Number of documents in results: " + TaatOr_list1.size() );
		br_output.newLine();
		br_output.write("Number of comparisons: " + compare);
		br_output.newLine();
	}
	
	private static void DaatOr(String[] line_term, BufferedWriter br_output,
			HashMap<String, LinkedList<Integer>> inverted_index) throws IOException {
		// TODO Auto-generated method stub
		
		HashMap<Integer, LinkedList<ListIterator<Integer>>> list_map = new HashMap<Integer, LinkedList<ListIterator<Integer>>>();
		LinkedList<Integer> postingsList = new LinkedList<Integer>();
		ListIterator<Integer> list_iter = null;
		ListIterator<Integer> list_iter1 = null;
		LinkedList<ListIterator<Integer>> temp = null;
		PriorityQueue<Integer> Prioritydoc = new PriorityQueue<Integer>(line_term.length);
		
		br_output.write("DaatOr");
		br_output.newLine();
		
		for(String term : line_term) {
			
			br_output.write(term);
			br_output.write(" ");
			
			if(inverted_index.containsKey(term)) {
				postingsList = inverted_index.get(term);
				list_iter = postingsList.listIterator();
				list_iter1 = postingsList.listIterator();
				list_iter1.next();
				Prioritydoc.add(postingsList.getFirst());
				Integer docid = null;
				while(list_iter.hasNext()) {
					docid = list_iter.next();
					if(list_map.containsKey(docid)) {
						temp = list_map.get(docid);
						temp.add(list_iter1);
						list_map.put(docid, temp);
					}else {
						temp = new LinkedList<ListIterator<Integer>>();
						temp.add(list_iter1);
						list_map.put(docid, temp);
					}
				}
			}
		}
		
		int docid, compare = 0;
		LinkedList<Integer> list_final = new LinkedList<Integer>();
		LinkedList<ListIterator<Integer>> temp_iter = new LinkedList<ListIterator<Integer>>();
		while(Prioritydoc.size() > 1) {
			docid = Prioritydoc.poll();
			compare++;
			if(!list_final.contains(docid)) {
				list_final.add(docid);
			}
			temp = list_map.get(docid);
			while(!temp.isEmpty()) {
				list_iter = temp.removeFirst();
				if(list_iter.hasNext()) {
					Prioritydoc.add(list_iter.next());
					temp_iter.add(list_iter);
					list_map.replace(docid, temp_iter);
				}
			}
			temp_iter = new LinkedList<ListIterator<Integer>>();
		}
		
		if(Prioritydoc.size() == 1) {
			docid = Prioritydoc.poll();
			if(!list_final.contains(docid)) {
				list_final.add(docid);
			}
			temp = list_map.get(docid);
			while(!temp.isEmpty()) {
				list_iter = temp.removeFirst();
				while(list_iter.hasNext()) {
					docid = list_iter.next();
					if(!list_final.contains(docid)) {
						list_final.add(docid);
					}
				}
			}
		}
		
		br_output.newLine();
		br_output.write("Results: ");
		if(list_final.isEmpty()) {
			br_output.write("empty");
		}else {
			for(Integer doc : list_final) {
				br_output.write(doc.toString());
				br_output.write(" ");
			}
		}
		br_output.newLine();
		br_output.write("Number of documents in results: " + list_final.size() );
		br_output.newLine();
		br_output.write("Number of comparisons: " + compare);
		br_output.newLine();
	}
	
	private static void DaatAnd(String[] line_term, BufferedWriter br_output,
			HashMap<String, LinkedList<Integer>> inverted_index) throws IOException {
		// TODO Auto-generated method stub
		
		HashMap<Integer, LinkedList<ListIterator<Integer>>> list1_map = new HashMap<Integer, LinkedList<ListIterator<Integer>>>();
		HashMap<Integer, LinkedList<myIterator>> list2_map = new HashMap<Integer, LinkedList<myIterator>>();
		LinkedList<Integer> postingsList = new LinkedList<Integer>();
		ListIterator<Integer> list_iter = null;
		ListIterator<Integer> list_iter1 = null;
		myIterator list_myiter = null;
		LinkedList<ListIterator<Integer>> temp = null;
		LinkedList<myIterator> temp1 = null;
		PriorityQueue<Integer> Prioritydoc = new PriorityQueue<Integer>(line_term.length);
		
		br_output.write("DaatAnd");
		br_output.newLine();
		
		for(String term : line_term) {
			
			br_output.write(term);
			br_output.write(" ");
			
			if(inverted_index.containsKey(term)) {
				postingsList = inverted_index.get(term);
				list_iter = postingsList.listIterator();
				list_iter1 = postingsList.listIterator();
				list_myiter = new myIterator(postingsList);
				list_iter1.next();
				Prioritydoc.add(postingsList.getFirst());
				Integer docid = null;
				while(list_iter.hasNext()) {
					docid = list_iter.next();
					if(list1_map.containsKey(docid)) {
						temp = list1_map.get(docid);
						temp1 = list2_map.get(docid);
						temp.add(list_iter1);
						temp1.add(list_myiter);
						list1_map.put(docid, temp);
						list2_map.put(docid, temp1);
					}else {
						temp = new LinkedList<ListIterator<Integer>>();
						temp1 = new LinkedList<myIterator>();
						temp.add(list_iter1);
						temp1.add(list_myiter);
						list1_map.put(docid, temp);
						list2_map.put(docid, temp1);
					}
				}
			}
		}
		
		int docid, compare = 0, jump = 0, temp_docid = 0, c=0;
		Object arr[] = null;
		LinkedList<Integer> list_final = new LinkedList<Integer>();
		LinkedList<ListIterator<Integer>> temp1_iter = new LinkedList<ListIterator<Integer>>();
		LinkedList<myIterator> temp2_iter = new LinkedList<myIterator>();
		
		while(Prioritydoc.size() == line_term.length) {
			docid = Prioritydoc.poll();
			compare++; 
			arr = Prioritydoc.toArray();
			for(Object id : arr) {
				if((Integer)id == docid) {
					c++;
				}
			}
			if(c == line_term.length-1) {
				list_final.add(docid);
				while(c != 0) {
					Prioritydoc.poll();
					c--;
				}
			}
			else if(c > 0) {
				while(c != 0) {
					Prioritydoc.poll();
					c--;
				}
			}
			temp = list1_map.get(docid);
			temp1 = list2_map.get(docid);
			while(!temp.isEmpty() && !temp1.isEmpty()) {
				list_myiter = temp1.removeFirst();
				list_iter = temp.removeFirst();
				jump = list_myiter.getJump();
				if(list_myiter.indexOf(docid) % jump == 0) {
					if(list_myiter.hasNext()) {
						compare++;
						temp_docid = list_myiter.next();
						Prioritydoc.add(temp_docid);
						if(Prioritydoc.peek() == temp_docid) {
							temp2_iter.add(list_myiter);
							list_iter = postingsList.listIterator(list_myiter.indexOf(docid));
							temp1_iter.add(list_iter);
						}else {
							Prioritydoc.remove(temp_docid);
							if(list_iter.hasNext()) {
								Prioritydoc.add(list_iter.next());
								temp1_iter.add(list_iter);
								temp2_iter.add(list_myiter);
							}
						}
					}else {
						if(list_iter.hasNext()) {
							Prioritydoc.add(list_iter.next());
							temp1_iter.add(list_iter);
							temp2_iter.add(list_myiter);
						}
					}
				}else {
					if(list_iter.hasNext()) {
						Prioritydoc.add(list_iter.next());
						temp1_iter.add(list_iter);
						temp2_iter.add(list_myiter);
					}
				}
			}
			list1_map.replace(docid, temp1_iter);
			list2_map.replace(docid, temp2_iter);
			temp1_iter = new LinkedList<ListIterator<Integer>>();
			temp2_iter = new LinkedList<myIterator>();
		}
		
		br_output.newLine();
		br_output.write("Results: ");
		if(list_final.isEmpty()) {
			br_output.write("empty");
		}else {
			for(Integer doc : list_final) {
				br_output.write(doc.toString());
				br_output.write(" ");
			}
		}
		br_output.newLine();
		br_output.write("Number of documents in results: " + list_final.size() );
		br_output.newLine();
		br_output.write("Number of comparisons: " + compare);
		br_output.newLine();
		
	}

	private static void getPostings(String[] line_term, BufferedWriter br_output,
			HashMap<String, LinkedList<Integer>> inverted_index) throws IOException {
		// TODO Auto-generated method stub
		
		LinkedList<Integer> postingsList = new LinkedList<Integer>();

		for (String term : line_term) {
			if(inverted_index.containsKey(term)) {
				postingsList = inverted_index.get(term);
				br_output.write("GetPostings");
				br_output.newLine();
				br_output.write(term);
				br_output.newLine();
				br_output.write("Postings list: ");
				for(Integer doc: postingsList) {
					br_output.write(doc.toString());
					br_output.write(" ");
				}
				br_output.newLine();
			}else {
				br_output.write("GetPostings");
				br_output.newLine();
				br_output.write(term);
				br_output.newLine();
				br_output.write("Postings List: empty");
				br_output.newLine();
			}
		}
	}

	private static LinkedList<Integer> mergeTwoLists(LinkedList<Integer> docs, LinkedList<Integer> temp) {
		// TODO Auto-generated method stub
		LinkedList<Integer> t = new LinkedList<Integer>();
		
		for(int index = 0; index<temp.size();index++) {
			t.add(temp.get(index));
		}
		
		for(int index = 0; index<docs.size();index++) {
			t.add(docs.get(index));
		}
		
		Collections.sort(t);
		
		return t;
	}

	@SuppressWarnings("unused")
	private static void printInvertedIndex(HashMap<String, LinkedList<Integer>> inverted_index) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

		for (Map.Entry<String, LinkedList<Integer>> postinglist : inverted_index.entrySet()) {
			if(postinglist.getValue().contains(7250))
				System.out.println(postinglist.getKey() + " : " + postinglist.getValue());
		}
		System.out.println("Index Size: " + inverted_index.size());
	}

	private static Terms getTerms(IndexReader reader, String fieldname) throws Exception {
		// TODO Auto-generated method stub
		Terms terms = MultiFields.getTerms(reader, fieldname);
		return terms;
	}

	private static IndexReader createReader() throws IOException {
		// TODO Auto-generated method stub

		Directory dir = FSDirectory.open(Paths.get(Indexdir));

		IndexReader reader = DirectoryReader.open(dir);

		return reader;
	}
}


// Iterator customized to implement Skip Pointer logic
class myIterator implements ListIterator<Integer>{
	
	LinkedList<Integer> list = new LinkedList<Integer>(); 
	int size;
	Integer current_node = null;
	int index;
	int jump;

	myIterator(LinkedList<Integer> list){
		this.list = list;
		this.size = list.size();
		this.index = 0;
		current_node = list.get(index);
		this.jump = (size / (int) Math.sqrt(size));
	}
	
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		if(list.isEmpty()) {
			return false;
		}else if((index+jump) >= size) {
			return false;
		}
		return true;
	}

	@Override
	public Integer next() {
		// TODO Auto-generated method stub
		index += jump;
		current_node = list.get(index);
		return current_node;
	}
	
	public Integer getIndex() {
		return index;
	}
	
	public Integer getJump() {
		return jump;
	}
	
	public Integer indexOf(Integer docid) {
		return list.indexOf(docid);
	}
	
	@Override
	public boolean hasPrevious() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int nextIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer previous() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int previousIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void set(Integer e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(Integer e) {
		// TODO Auto-generated method stub
		
	}
	
}


