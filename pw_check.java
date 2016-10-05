/*
Ben Morgenstern - 39853950
bdm46@pitt.edu
CS 1501 - Assignment #1
*/

import java.io.*;
import java.util.*;

public class pw_check
{
	private char [] array = {'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '2', '3', '5', '6', '7', '8', '9', '!', '@', '$', '^', '_', '*'};
	int nL = 0, prefixIndex = 0;
	long startTime = 0, findTime = 0, timeDiff = 0;
	boolean found = false, write = false;
	private Node firstInvalid;
	private	Node firstNode = new Node();
	private	Node holdFirst = firstNode;
	private boolean search = false;
	private Node searchNode = null;
	
	private String checkPass;
	
	public static void main(String args[])  throws FileNotFoundException, UnsupportedEncodingException
	{
		pw_check run = new pw_check();
		run.pw_check(args);		
	}
	
	public void pw_check(String args[])  throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean newPass = true;
		if(args[0].equals("-find"))
		{
			File file = new File("dictionary.txt");
			Scanner fileScan = null;
			try{
				fileScan = new Scanner(file);
			}catch(FileNotFoundException e){}
			firstInvalid = buildInvalidDLB(fileScan); //create DLB of pruned words in dictionary.txt
			buildValidDLB(); //using 5 nested for-loops, it creates a StringBuilder
										  //of every possible combination of characters (38^5 total combinations).
										  //Before attempting to add the string to the DLB, it checks to see if the string
										  //has a valid amount of chars/ints/symbols in the string. Only if all 3 have valid amounts
										  //does it attempt to add to the DLB
		}
		else if(args[0].equals("-check"))
		{
			File searchFile = new File("all_passwords.txt");
			if(!searchFile.exists())
			{
				System.out.println("allPasswords.txt does not exist. Please run the -find argument first.");
			}
			else
			{
				Scanner fileScan = new Scanner(searchFile);
				Scanner inScan = new Scanner(System.in);
				do
				{
					System.out.println("What password would you like to check?");
					checkPass = inScan.nextLine();
					while(fileScan.hasNextLine())
					{
						String preSplit = fileScan.nextLine();
						String [] passArr = preSplit.split(",");
						buildExistDLB(passArr[0], passArr[1]);
						nL++;
					}
					search = false;
					searchNode = null;
					found = false;
					System.out.println();
					searchNode = searchDLB(checkPass, firstNode);
					if(searchNode == null)
					{
						System.out.println("Password not found");
						for(int i = 0; i <= checkPass.length(); i++)
						{
							String prefix = checkPass.substring(0, i);
							searchNode = searchDLB(prefix, firstNode);
							if(searchNode != null)
								prefixIndex++;
						}
						if(prefixIndex > 0)
						{
							System.out.println("The longest prefix found was: " + checkPass.substring(0, prefixIndex));
							System.out.println("Similar passwords: ");
							String prefix = checkPass.substring(0, prefixIndex);
							findSimilar(prefix, firstNode);
						}
						else
						{
							System.out.println("No prefix found");
						}
					}
					else
					{
						System.out.println(checkPass + " found! It took " + searchNode.time + " ms to generate.");
					}
					System.out.println("Would you like to search for another password? yes/no");
					String s = inScan.nextLine();
					if(s.equals("yes"))
						search = true;
					else if (!s.equals("no"))
					{
						while(!s.equals("no") && !s.equals("yes"))
						{
							System.out.println("Please enter either yes or no");
							s = inScan.nextLine();
							if(s.equals("yes"))
								search = true;
						}
					}
				}while(search);
			}
			
		}
		else if(args[0].equals("-test"))
		{
			File file = new File("dictionary.txt");
			Scanner fileScan = null;
			Scanner inScan = new Scanner(System.in);
			try{
				fileScan = new Scanner(file);
			}catch(FileNotFoundException e){}
			firstInvalid = buildInvalidDLB(fileScan);
			//System.out.println(firstInvalid.sibling.data);
			//System.out.println(firstInvalid.sibling.child.data);
			//System.out.println(firstInvalid.child.child.data);
			//System.out.println(firstInvalid.child.child.child.data);
			String s = inScan.nextLine();
			boolean testSearch = false;
			for(int i = 0; i < s.length()+1; i++)
			{
				String s2 = s.substring(0, i);
				testSearch = searchInvalid(s2, firstInvalid);
				System.out.println(s2 + " " + testSearch);
			}
		}
		else
			System.out.println("Command line argument invalid. Please run again with a valid input");
	}
	public Node buildInvalidDLB(Scanner fileScan)
	{
		String word;
		Node invalid = new Node();
		Node holdFirst = invalid;
		while(fileScan.hasNextLine())
		{
			word = fileScan.nextLine();
			word = word.toLowerCase();
			word += '#';
			if(word.indexOf('a') < 0 && word.indexOf('i') < 0 && word.length() < 7) //prunes words that aren't valid to begin with
			{
				//System.out.println(word);
				for(int i = 0; i < word.length(); i++)
				{
					if(!invalid.hasData())
					{
						invalid.data = word.charAt(i);
						Node newNode = new Node();
						invalid.child = newNode;
						invalid = invalid.child;
					}
					else if(invalid.hasData() && invalid.data == word.charAt(i))
					{
						if(invalid.hasChild())
							invalid = invalid.child;
						else
						{
							Node newNode = new Node();
							invalid.child = newNode;
							invalid = invalid.child;
						}
					}
					else if(invalid.hasData() && invalid.data != word.charAt(i))
					{
						if(invalid.hasSibling())
						{
							invalid = invalid.sibling;
							i--;
						}
						else
						{
							//System.out.println("Setting new parent of char " + word.charAt(i));
							Node newNode = new Node(word.charAt(i));
							invalid.sibling = newNode;
							invalid = invalid.sibling;
							Node newChild = new Node();
							invalid.child = newChild;
							invalid = invalid.child;
						}
						
					}
				}
				//System.out.println(word);
				invalid = holdFirst;
			}
		}
		return invalid;
	}
	public void buildValidDLB() throws FileNotFoundException, UnsupportedEncodingException
	{	
		PrintWriter pw = new PrintWriter("all_passwords.txt");
		StringBuilder pass = new StringBuilder();
		startTime = System.currentTimeMillis();
		for(int i = 0; i < 38; i++)
		{
			for(int j = 0; j < 38; j++)
			{
				for(int k = 0; k < 38; k++)
				{
					for(int l = 0; l < 38; l++)
					{
						for(int m = 0; m < 38; m++)
						{
							pass.append(array[i]);
							pass.append(array[j]);
							pass.append(array[k]);
							pass.append(array[l]);
							pass.append(array[m]);
							if(!hasMaxNum(pass) && !hasMaxLet(pass) && !hasMaxSym(pass)) //if valid number of numbers, letters, and symbols, send pass to the buildDLB function
							{
								write = buildDLB(pass); //buildDLB() returns boolean indicating wether or not the string passed in was successfully added to the DLB
								if(write) //if the word was added, take the difference in times and write out to file the string and the time difference
								{
									nL++;
									timeDiff = findTime - startTime;
									pw.println(pass + "," + timeDiff);
								}

								write = false;
							}
							pass = new StringBuilder(); //reset stringbuilder object
						}
					}
				}
			}
		}
		pw.close();
	}
	public boolean searchInvalid(String str, Node fNode)
	{
		//System.out.println(str.length());
		for(int i = 0; i < str.length(); i++)
		{
			if(fNode.hasData() && fNode.data == '#')
			{
				return true;
			}
			if((fNode.hasData() && fNode.data == str.charAt(i)) || (str.charAt(i) == '7' && fNode.hasData() && fNode.data == 't') || (str.charAt(i) == '0' && fNode.hasData() && fNode.data == 'o') || (str.charAt(i) == '3' && fNode.hasData() && fNode.data == 'e') || (str.charAt(i) == '$' && fNode.hasData() && fNode.data == 's'))
			{
				//System.out.println(str.charAt(i) + " found with " + fNode.data);
				fNode = fNode.child;
			}
			else if(fNode.hasData() && fNode.data != str.charAt(i))
			{
				if(fNode.hasSibling())
				{
					//System.out.println(fNode.data + " sibling is " + fNode.sibling.data);
					fNode = fNode.sibling;
					i--;
				}
				else
				{
					return false;
				}
			}
			
		}
		if(fNode.hasData() && fNode.data == '#')
			return true;
		return false;
	}
	public Node searchDLB(String str, Node fNode)
	{
		for(int i = 0; i < str.length(); i++)
		{
			if((fNode.hasData() && fNode.hasTime() && str.charAt(i) == fNode.data) || (fNode.hasData() && str.charAt(i) == fNode.data && i == str.length()-1))
			{
				//System.out.println(str.charAt(i) + " found--returning");
				return fNode;
			}
			if((fNode.hasData() && fNode.data == str.charAt(i)))
			{
				//System.out.println(str.charAt(i) + " found--iterating");
				fNode = fNode.child;
			}
			else if(fNode.hasData() && fNode.data != str.charAt(i))
			{
				if(fNode.hasSibling())
				{
					fNode = fNode.sibling;
					i--;
				}
				else
				{
					return null;
				}
			}
			
		}
		return null;
	}
	public void findSimilar(String pre, Node fNode)
	{
		Node siblingNode = null;
		int length = pre.length();
		for(int i = 0; i < pre.length(); i++)
		{
			if(fNode.hasData() && fNode.data ==  pre.charAt(i))
			{
				//System.out.println("found " + pre.charAt(i));
				fNode = fNode.child;
				siblingNode = fNode.sibling;
			}
			else
			{
				fNode = fNode.sibling;
				i--;
			}
		}
		//System.out.println(siblingNode.data);
		for(int i = 0; i < 5 - pre.length(); i++)
		{
			//System.out.println("adding " + fNode.data);
			pre += fNode.data;
			fNode = fNode.child;
		}
		for(int i = 0; i < 10; i++)
		{
			pre += fNode.data;
			System.out.println(pre + " was generated in " + fNode.time + " ms");
			pre = pre.substring(0, pre.length()-1);
			if(fNode.hasSibling())
				fNode = fNode.sibling;
			else
			{
				pre = pre.substring(0, length);
				fNode = siblingNode;
				for(int j = 0; j < 5 - pre.length(); j++)
				{
					//System.out.println("adding " + fNode.data);
					pre += fNode.data;
					fNode = fNode.child;
				}
			}
		}
	}
	public boolean buildDLB(StringBuilder str)
	{
		found = false;
		String subStr;
		for(int i = 0; i < 5; i++) //before adding to the DLB, check the word and all of it's substrings to see if they are one of the words in dictionary.txt
		{
			for(int j = i+1; j < 6; j++)
			{
				subStr = str.substring(i, j);
				found = searchInvalid(subStr, firstInvalid);
				if(found) //if a word is found in the dictionary.txt DLB, return false
				{
					//System.out.println(subStr + " found");
					return false;
				}
			}
		}
		if(!found) //if the word was not found, then it can be added to the DLB
		{
			for(int i = 0; i < 5; i ++)
			{
				if(!firstNode.hasData())
				{
					firstNode.data = str.charAt(i);
					if( i != 4)
					{
						Node newNode = new Node();
						firstNode.child = newNode;
						firstNode = firstNode.child;
					}
				}
				else if(firstNode.hasData() && firstNode.data == str.charAt(i))
				{
					if(firstNode.hasChild())
						firstNode = firstNode.child;
					else
					{
						Node newNode = new Node();
						firstNode.child = newNode;
						firstNode = firstNode.child;
					}
				}
				else if(firstNode.hasData() && firstNode.data != str.charAt(i))
				{
					if(firstNode.hasSibling())
					{
						firstNode = firstNode.sibling;
						i--;
					}
					else
					{
						Node newNode = new Node(str.charAt(i));
						firstNode.sibling = newNode;
						firstNode = firstNode.sibling;
						if(i != 4)
						{
							Node newChild = new Node();
							firstNode.child = newChild;
							firstNode = firstNode.child;	
						}
						
					}
								
				}
			}
			findTime = System.currentTimeMillis();
			firstNode.time = findTime - startTime;
			firstNode = holdFirst;
			
			return true;	
		}
		return false;
	}
	public void buildExistDLB(String str, String time)
	{
		for(int i = 0; i < 5; i ++)
		{
			if(!firstNode.hasData())
			{
				firstNode.data = str.charAt(i);
				if( i != 4)
				{
					Node newNode = new Node();
					firstNode.child = newNode;
					firstNode = firstNode.child;
				}
			}
			else if(firstNode.hasData() && firstNode.data == str.charAt(i))
			{
				if(firstNode.hasChild())
					firstNode = firstNode.child;
				else
				{
					Node newNode = new Node();
					firstNode.child = newNode;
					firstNode = firstNode.child;
				}
			}
			else if(firstNode.hasData() && firstNode.data != str.charAt(i))
			{
				if(firstNode.hasSibling())
				{
					firstNode = firstNode.sibling;
					i--;
				}
				else
				{
					Node newNode = new Node(str.charAt(i));
					firstNode.sibling = newNode;
					firstNode = firstNode.sibling;
					if(i != 4)
					{
						Node newChild = new Node();
						firstNode.child = newChild;
						firstNode = firstNode.child;	
					}
					
				}
							
			}
		}
		firstNode.time = Double.parseDouble(time);
		firstNode = holdFirst;
	}
	public boolean hasMaxNum(StringBuilder str)
	{
		int numCount = 0;
		for(int i = 0; i < str.length(); i++)
		{
			int ascii = (int) str.charAt(i);
			if(ascii >= 48 && ascii <= 57)
				numCount++;
		}
		if(numCount > 2 || numCount <= 0)
			return true;
		return false;
	}
	public boolean hasMaxSym(StringBuilder str)
	{
		int symCount = 0;
		for(int i = 0; i < str.length(); i++)
		{
			int ascii = (int) str.charAt(i);
			if((ascii >= 33 && ascii <= 46) || (ascii >= 58 && ascii <= 64) || (ascii >= 91 && ascii <= 96))
				symCount++;
		}
		if(symCount > 2 || symCount <= 0)
			return true;
		return false;
	}
	public boolean hasMaxLet(StringBuilder str)
	{
		int letCount = 0;
		for(int i = 0; i < str.length(); i++)
		{
			int ascii = (int) str.charAt(i);
			if(ascii >= 97 && ascii <= 122)
				letCount++;
		}
		if(letCount > 3 || letCount <= 0)
			return true;
		return false;
	}
	class Node
	{
		public Node sibling;
		public Node child;
		public char data;
		public double time;
		
		public Node()
		{
			time = 0;
			sibling = null;
			child = null;
			data = '\0';
		}
		public Node(char key)
		{
			sibling = null;
			child = null;
			data = key;
		}
		public boolean hasSibling()
		{
			if(sibling != null)
				return true;
			return false;
		}
		public boolean hasChild()
		{
			if(child != null)
				return true;
			return false;
		}
		public boolean hasData()
		{
			if(data != '\0')
				return true;
			return false;
		}
		public boolean hasTime()
		{
			if(time != 0)
				return true;
			return false;
		}
	}
}