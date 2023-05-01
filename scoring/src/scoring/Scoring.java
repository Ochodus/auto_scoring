package scoring;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import classLoader.FileClassLoader;

class InputGenerator {
	private Random rand = null;
	
	public InputGenerator() {
		this.rand = new Random();
	}
	
	public InputGenerator(int seed) {
		this.rand = new Random(seed);
	}
	
	public int generateOneInt(int range) {
		return rand.nextInt(range);
	}
	
	public double generateOneDouble(int range) {
		return rand.nextDouble(range);
	}
	
	public int[] generateInt(int testSize, int range) {
		int[] arr = new int[testSize];
		for (int i = 0; i < testSize; i++) {
			arr[i] = rand.nextInt(range);
		}
		return arr;
	}
	
	public int[][] generateIntArr(int testSize, int range, int maxArrSize) {
		int[][] arr = new int[testSize][];
		for (int i = 0; i < testSize; i++) {
			int arrSize = rand.nextInt(maxArrSize) + 1;
			arr[i] = new int[arrSize];
			for (int j = 0; j < arrSize; j++) {
				arr[i][j] = rand.nextInt(range);
			}
		}
		return arr;
	}
	
	public String[][] generateStringArr(int testSize, int maxArrSize) {
		int leftLimit = 97;
		int rightLimit = 122;
		int targetStringLength = 10;
		
		String[][] arr = new String[testSize][];
		
		for (int i = 0; i < testSize; i++) {
			int arrSize = rand.nextInt(maxArrSize) + 1;
			arr[i] = new String[arrSize];
			for (int j = 0; j < arrSize; j++) {
				StringBuilder buffer = new StringBuilder(targetStringLength);
				for (int s = 0; s < targetStringLength; s++) {
					int randomLimitedInt = leftLimit + (int)(rand.nextFloat() * (rightLimit - leftLimit +  1));
					buffer.append((char) randomLimitedInt);
				}
				String generatedString = buffer.toString();
				arr[i][j] = generatedString;
			}
		}
		
		return arr;
	}
	
	public double[][] generateDoubleArr(int testSize, int range, int maxArrSize) {
		double[][] arr = new double[testSize][];
		for (int i = 0; i < testSize; i++) {
			int arrSize = rand.nextInt(maxArrSize) + 1;
			arr[i] = new double[arrSize];
			for (int j = 0; j < arrSize; j++) {
				arr[i][j] = rand.nextDouble(range);
			}
		}
		return arr;
	}
}

class MethodsFinder {
	private String rootPath = null;
	private String pkgName = null;
	
	private String classNum = null;
	private String stdName = null;
	private String stdNum = null;
	private String[] targetClass = null;
	protected String[] targetMethods = null;
	private Boolean verbose = false;
	
	private FileClassLoader fcLoader;
	private ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
	private ArrayList<Method> methodList = new ArrayList<Method>();
	
	private InputGenerator ig = new InputGenerator(100);
	
	public Constructor<?>[] cons = null;
	public Object[] objs = null;
	
	public MethodsFinder(String rootPath, String hwNum, String pkgName, String classNum, String stdName, String stdNum, String[] targetClass, String[] targetMethods, Boolean verbose) {
		this.rootPath = rootPath;
		this.pkgName = pkgName;
		
		this.classNum = classNum;
		this.stdName = stdName;
		this.stdNum = stdNum;
		
		this.targetClass = targetClass;
		this.targetMethods = targetMethods;
		
		this.verbose = verbose;
		
		this.fcLoader = new FileClassLoader();
		
		this.loadClass();
		this.getMethods();
		this.initConstructors();
	}
	
	private void loadClass() {
		for (int i = 0; i < this.targetClass.length; i++) {
			try {
				classList.addAll(fcLoader.getClass(rootPath + classNum + "\\" + stdName + " " + stdNum, targetClass));
				
			} catch(ClassNotFoundException e) {
				if (verbose) {
					System.out.print(this.stdNum + ": ");
					System.out.println(e);
				}
			} catch(NoClassDefFoundError e) {
				if (verbose) {
					System.out.print(this.stdNum + ": ");
					System.out.println(e);
				}
			} catch(Exception e) {
				if (verbose) {
					System.out.print(this.stdNum + ": ");
					System.out.println(e);
				}
			}
		}
	}
	
	private Boolean checkMethodValidation(Method m) {
		for (String methodName : this.targetMethods) {
			if (methodName.equals(m.getName())) {
				return true;
			}
		}
		return false;
	}
	
	private void getMethods() {
		try {
			for (Class<?> clazz : this.classList) {
				Method[] methods = clazz.getMethods();
				for (Method method: methods) {
					if (checkMethodValidation(method)) {
						this.methodList.add(method);
					}
				}
			}
		} catch(NullPointerException e) {
			
		}
	}
	
	public Method getMethod(String methodName) {
		if (this.methodList != null) {
			for(Method m: this.methodList) {
				if (methodName.equals(m.getName())) {
					return m;
				}
			}
		}
		return null;
	}
	
	public Class<?> getClazz(String className) {
		if (this.classList != null ) {
			for (Class<?> c : this.classList) {
				if (className.equals(c.getName())) {
					return c;
				}
			}
		}
		return null;
	}
	
	public void initConstructors() {
		if (this.getClazz("cse2010.hw2.Poly") != null) this.cons = this.getClazz("cse2010.hw2.Poly").getConstructors();
	}
	
	public Object createInstance(int consNum, Object args) {
		Object inst = null;
		try {
			inst = this.cons[0].newInstance(args);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return inst;
	}
	
	public void createRandInstances(int len) {
		Object[] inst = new Object[len];
		for (int i = 0; i < len; i++) {
			try {
				int maxTermLen = ig.generateOneInt(15) + 1;
				inst[i] = (Object)this.cons[0].newInstance(maxTermLen);
				
				System.out.println(maxTermLen);
				int addTermLen = ig.generateOneInt(maxTermLen);
				for (int j = 0; j < addTermLen; j++) {
					int coef = ig.generateOneInt(15);
					
					Method addTerm = this.getMethod("addTerm");
					
					addTerm.invoke(inst[i], coef, j);
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		this.objs = inst;
	}
	
	public String getClassNum() {
		return this.classNum;
	}
	
	public String getName() {
		return this.stdName;
	}
	
	public String getId() {
		return this.stdNum;
	}
}

class Solver extends MethodsFinder {
	private HashMap<String, Boolean> scoreMap = new HashMap<>();
	private HashMap<String, Integer> weightedScore = null;
	
	public Solver(String rootPath, String hwNum, String pkgName, String classNum, String name, String stdNum, String[] targetClass,
			String[] targetMethods, Boolean verbose, HashMap<String, Integer> weightedScore) {
		super(rootPath, hwNum, pkgName, classNum, name, stdNum, targetClass, targetMethods, verbose);
		
		this.weightedScore = weightedScore;
	}
	
	public double getTotalScore() {
		double score = 0.0;
		double maxScore = 0.0;
		for (String methodName : this.targetMethods) {
			maxScore += weightedScore.get(methodName);

			if (scoreMap.get(methodName) != null && this.scoreMap.get(methodName)) score += weightedScore.get(methodName);
		}
		
		return Math.round((score/maxScore) * 10000)/100.0;
	}
	
	public void addScore(String methodName, Boolean correctness) {
		this.scoreMap.put(methodName, correctness);
	}
	
	public void printScore(String methodName) {
		if (this.scoreMap.get(methodName) == null || !this.scoreMap.get(methodName)) System.out.println(methodName + ": Wrong");
		else System.out.println(methodName + ": Correct");
	}
}

class Scorer {
	
	double[] scores = null;
	Boolean[][] scores_details = null;
	
	String rootPath = "";
	String hwNum = "";
	
	String[] stClass = null;
	String[] stNames = null;
	String[] stNums = null;
	
	String pkgName = "";
	String[] className = null;
	String[] targetMethods = null;
	
	InputGenerator ig = new InputGenerator(100);
	
	Boolean verbose = false;
	
	MethodsFinder teacher = null;
	ArrayList<Solver> students = new ArrayList<Solver>();
	
	HashMap<String, Integer> weightedScore = null;;
	
	public Scorer(String rootPath, String hwNum, String[] stClass, String[] stNames, String[] stNums, String pkgName, String[] className, String[] targetMethods, HashMap<String, Integer> weightedScore, Boolean verbose) {
		this.rootPath = rootPath;
		this.hwNum = hwNum;
		
		this.stClass = stClass;
		this.stNames = stNames;
		this.stNums = stNums;
		
		this.pkgName = pkgName;
		this.className = className;
		this.targetMethods = targetMethods;
		
		this.weightedScore = weightedScore;
		
		this.verbose = verbose;
		
		create_teacher();
		create_students();
	}
	
	private void create_teacher() {
		for (int i = 0; i < this.stClass.length; i++) {
			if (this.stClass[i].equals("T")) {
				this.teacher = new MethodsFinder(this.rootPath, this.hwNum, this.pkgName, this.stClass[i], this.stNames[i], this.stNums[i], this.className, this.targetMethods, this.verbose);
			}
		}
	}
	
	private void create_students() {
		for (int i = 0; i < this.stClass.length; i++) {
			Solver std = new Solver(this.rootPath, this.hwNum, this.pkgName, this.stClass[i], this.stNames[i], this.stNums[i], this.className, this.targetMethods, this.verbose, this.weightedScore);
			
			students.add(std);
		}
	}
	
	public void test_all() {
		for (Solver std : this.students) {
			testClass(std);
		}
	}
	
	public void testClass(Solver std) {
		if (verbose) {
			System.out.println("Scoring student: " + std.getId());
			System.out.println("===================================================\n");
		}
		
		for (String methodName : targetMethods) {
		
			ExecutorService executor = Executors.newSingleThreadExecutor();
			
			Future<Integer> future = executor.submit(() -> {
				testMethod(std, methodName);
				
				return 0;
			});
			
			try {
				future.get(1000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				future.cancel(true);
			} catch (TimeoutException e) {
				future.cancel(true);
				System.out.println(e);
				std.addScore(methodName, false);
			}
		}
		
		if (verbose) System.out.println("--------------------------------------------------------\n");
	}
	
	public void testMethod(Solver std, String methodName) throws InstantiationException, IllegalArgumentException, NoSuchMethodException, SecurityException, ClassNotFoundException, MalformedURLException {
		if (verbose) System.out.println("Current method: " + methodName);
		System.out.println("-----------------------------------------------------\n");
		
		Boolean acc = true;
		Method targetM = teacher.getMethod(methodName);
		Method outputM = std.getMethod(methodName);
		
		int instanceNum = 10;
		
		Object[] inst = new Object[instanceNum];
		Object[] instS = new Object[instanceNum];
		
		Method toString = teacher.getMethod("toString");
		Method toStringS = std.getMethod("toString");
		
		int[] exp = new int[instanceNum];
		
		Boolean addTermCor = true;
		
		for (int i = 0; i < instanceNum; i++) {
			try {
				int maxTermLen = ig.generateOneInt(13) + 3;
				inst[i] = (Object)teacher.cons[0].newInstance(maxTermLen);
				instS[i] = (Object)std.cons[0].newInstance(maxTermLen);
				
				exp[i] = maxTermLen;
				for (int j = 0; j < maxTermLen; j++) {
					int coef = ig.generateOneInt(15);
					
					Method addTerm = teacher.getMethod("addTerm");
					Method addTermS = std.getMethod("addTerm");
					
					if (coef > 0) {
						addTerm.invoke(inst[i], coef, j);
						addTermS.invoke(instS[i], coef, j);
					}
				}
				
				if (!toString.invoke(inst[i]).equals(toStringS.invoke(instS[i]))) addTermCor = false;
				
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NullPointerException e) {
				//e.printStackTrace();
			}
		}
		
		
		
		teacher.objs = inst;
		std.objs = instS;
		
		if (targetM != null && outputM != null) {
			try {
				switch (targetM.getName()) {
				case "degree":
					acc = true;
					
					for (int j = 0; j < std.objs.length; j++) {
						int output = (int)outputM.invoke(std.objs[j]);
						int answer = (int)targetM.invoke(teacher.objs[j]);
						
						String studentObj = (String)toStringS.invoke(std.objs[j]);
						String teacherObj = (String)toString.invoke(teacher.objs[j]);
						
						if (verbose) {
							System.out.print("Method Inputs: ");
							System.out.println("Teacher - (" + teacherObj + ") / Student - (" + studentObj + ")");
							
							System.out.print("Method outputs: " + output + "\n");

							System.out.print("Target outputs: " + answer + "\n");
							
							if (output != answer) acc = false;
							System.out.println();
						}
						else { if (output != answer) acc = false; }
					}
					
					std.addScore(targetM.getName(), acc ? true : false);
					
					if (verbose) System.out.println("Evaluation result of " + methodName + ": " + (acc ? "Correct" : "Incorrect"));
					break;
				case "getTermCount":
					acc = true;
					
					for (int j = 0; j < std.objs.length; j++) {
						int output = (int)outputM.invoke(std.objs[j]);
						int answer = (int)targetM.invoke(teacher.objs[j]);
						
						String studentObj = (String)toStringS.invoke(std.objs[j]);
						String teacherObj = (String)toString.invoke(teacher.objs[j]);
						
						if (verbose) {
							System.out.print("Method Inputs: ");
							System.out.println("Teacher - (" + teacherObj + ") / Student - (" + studentObj + ")");
							
							System.out.print("Method outputs: " + output + "\n");

							System.out.print("Target outputs: " + answer + "\n");
							
							if (output != answer) acc = false;
							System.out.println();
						}
						else { if (output != answer) acc = false; }
					}
					
					std.addScore(targetM.getName(), acc ? true : false);
					
					if (verbose) System.out.println("Evaluation result of " + methodName + ": " + (acc ? "Correct" : "Incorrect"));
					break;
				case "getCoefficient":
					acc = true;
					
					for (int j = 0; j < std.objs.length; j++) {
						
						int exponential = ig.generateOneInt(exp[j]);
						
						int output = (int)outputM.invoke(std.objs[j], exponential);
						int answer = (int)targetM.invoke(teacher.objs[j], exponential);
						
						String studentObj = (String)toStringS.invoke(std.objs[j]);
						String teacherObj = (String)toString.invoke(teacher.objs[j]);
						
						if (verbose) {
							System.out.print("Method Inputs: ");
							System.out.println("Teacher - (" + teacherObj + ") / Student - (" + studentObj + ") / Exponent - " + exponential);
							
							System.out.print("Method outputs: " + output + "\n");

							System.out.print("Target outputs: " + answer + "\n");
							
							if (output != answer) acc = false;
							System.out.println();
						}
						else { if (output != answer) acc = false; }
					}
					
					std.addScore(targetM.getName(), acc ? true : false);
					
					if (verbose) System.out.println("Evaluation result of " + methodName + ": " + (acc ? "Correct" : "Incorrect"));
					break;
				case "addTerm":
					for (int j = 0; j < std.objs.length; j++) {

						String studentObj = (String)toStringS.invoke(std.objs[j]);
						String teacherObj = (String)toString.invoke(teacher.objs[j]);
						
						if (verbose) {
							System.out.print("Method Inputs: Skip\n");

							System.out.print("Method outputs: " + studentObj + "\n");

							System.out.print("Target outputs: " + teacherObj + "\n\n");
						}
					}
					
					std.addScore(targetM.getName(), addTermCor ? true : false);
					
					if (verbose) System.out.println("Evaluation result of " + methodName + ": " + (addTermCor ? "Correct" : "Incorrect"));
					break;
				case "add":
					acc = true;
					
					for (int j = 0; j < std.objs.length - 1; j++) {
						
						String inputObjS = (String)toStringS.invoke(std.objs[j+1]);
						//String inputObj = (String)toString.invoke(teacher.objs[j+1]);
						
						String studentObj = (String)toStringS.invoke(std.objs[j]);
						String teacherObj = (String)toString.invoke(teacher.objs[j]);
						
						Object output = (Object)outputM.invoke(std.objs[j], (Object)(std.objs[j+1]));
						Object target = (Object)targetM.invoke(teacher.objs[j], (Object)(teacher.objs[j+1]));

						
						String studentObjAfter = (String)std.getMethod("toString").invoke(output);
						String teacherObjAfter = (String)toString.invoke(target);
						
						if (verbose) {
							System.out.print("Method Inputs: ");
							System.out.println("Teacher - (" + teacherObj + ") / Student - (" + studentObj + ") / Poly - " + inputObjS);
							
							System.out.print("Method outputs: " + studentObjAfter + "\n");

							System.out.print("Target outputs: " + teacherObjAfter + "\n");
							
							if (studentObjAfter.compareTo(teacherObjAfter) != 0) acc = false;
							System.out.println();
						}
						else { if (studentObjAfter.compareTo(teacherObjAfter) != 0) acc = false; }
					}
					
					std.addScore(targetM.getName(), acc ? true : false);
					
					if (verbose) System.out.println("Evaluation result of " + methodName + ": " + (acc ? "Correct" : "Incorrect"));
					break;
				case "mult":
					acc = true;
					
					for (int j = 0; j < std.objs.length - 1; j++) {
						
						String inputObjS = (String)toStringS.invoke(std.objs[j+1]);
						//String inputObj = (String)toString.invoke(teacher.objs[j+1]);
						
						String studentObj = (String)toStringS.invoke(std.objs[j]);
						String teacherObj = (String)toString.invoke(teacher.objs[j]);
						
						Object output = (Object)outputM.invoke(std.objs[j], (Object)(std.objs[j+1]));
						Object target = (Object)targetM.invoke(teacher.objs[j], (Object)(teacher.objs[j+1]));

						
						String studentObjAfter = (String)std.getMethod("toString").invoke(output);
						String teacherObjAfter = (String)toString.invoke(target);
						
						if (verbose) {
							System.out.print("Method Inputs: ");
							System.out.println("Teacher - (" + teacherObj + ") / Student - (" + studentObj + ") / Poly - " + inputObjS);
							
							System.out.print("Method outputs: " + studentObjAfter + "\n");

							System.out.print("Target outputs: " + teacherObjAfter + "\n");
							
							if (studentObjAfter.compareTo(teacherObjAfter) != 0) acc = false;
							System.out.println();
						}
						else { if (studentObjAfter.compareTo(teacherObjAfter) != 0) acc = false; }
					}
					
					std.addScore(targetM.getName(), acc ? true : false);
					
					if (verbose) System.out.println("Evaluation result of " + methodName + ": " + (acc ? "Correct" : "Incorrect"));
					break;
				case "eval":
					acc = true;
					
					for (int j = 0; j < std.objs.length - 1; j++) {
						
						double input = Math.round(ig.generateOneDouble(20) * 100) / 100.00;
						
						String studentObj = (String)toStringS.invoke(std.objs[j]);
						String teacherObj = (String)toString.invoke(teacher.objs[j]);
						
						Double output = Math.round((double)outputM.invoke(std.objs[j], input) * 100) / 100.00;
						Double target = Math.round((double)targetM.invoke(teacher.objs[j], input) * 100) / 100.00;

						if (verbose) {
							System.out.print("Method Inputs: ");
							System.out.println("Teacher - (" + teacherObj + ") / Student - (" + studentObj + ") / X value - " + input);
							
							System.out.print("Method outputs: " + output + "\n");

							System.out.print("Target outputs: " + target + "\n");
							
							if (output.compareTo(target) != 0) acc = false;
							System.out.println();
						}
						else { if (output.compareTo(target) != 0) acc = false; }
					}
					
					std.addScore(targetM.getName(), acc ? true : false);
					
					if (verbose) System.out.println("Evaluation result of " + methodName + ": " + (acc ? "Correct" : "Incorrect"));
					break;
				}
			} catch (InvocationTargetException e) {
				if (verbose) {
					System.out.print(std.getId() + ": ");
					System.out.println(e.getCause());
				}
			} catch (IllegalAccessException e) {
				if (verbose) System.out.println(e);
			}
			if (verbose) System.out.println("--------------------------------------------------------\n");
		}
		else if (outputM == null){
			if (verbose) System.out.println("Cannot find method: " + methodName);
			std.addScore(methodName, false);
			if (verbose) System.out.println("--------------------------------------------------------\n");
		}
	}
	
	
	public static void printArray(String[] arr) {
		for (String s : arr) {
			System.out.print(s + " ");
		}
		System.out.println();
	}
	
	public static void printArray(int[] arr) {
		for (int i : arr) {
			System.out.print(i + " ");
		}
		System.out.println();
	}
	
	public static void printArray(double[] arr) {
		for (double d : arr) {
			System.out.print(Math.round(d * 100)/100.0 + " ");
		}
		System.out.println();
	}
	
	public void printAllScore() {
		System.out.println("Print All Student's Scores...");
		System.out.println("===================================================");
		for (Solver std : this.students) {
			printScore(std);
		}
	}
	
	public void printScore(Solver std) {
		System.out.println("Class: " + std.getClassNum() + " | Student Name: " + std.getName() + " | ID: " + std.getId());
		System.out.println("-------------------------------------");
		
		for (String methodName : this.targetMethods) {
			std.printScore(methodName);
		}
		System.out.println("Total Score: " + std.getTotalScore());
		
		System.out.println("\n");
	}
}

class LongRunningTask implements Runnable {
	Scorer sc = null;
	Solver std = null;
	String methodName = null;
	
	public LongRunningTask(Scorer sc, Solver std, String methodName) {
		this.sc = sc;
		this.std = std;
		this.methodName = methodName;
	}
	
	@Override
    public void run() {
		
        
    }
}

class InvokeThread implements Runnable {
	@Override
	public void run() {
		
	}
}

public class Scoring {
	
	static String rootPath = "C:\\Users\\DBLab2\\Desktop\\Data_structure_assignment\\Submission\\";
	static String[] classNum = {"A", "B", "T"};
	static String hwNum = "hw2\\";
	
	static String pkgName = "cse2010.hw2.";
	static String[] className = {"cse2010.hw2.Poly", "cse2010.hw2.Term", "cse2010.hw2.Polynomial"};
	static String[] targetMethods = {"degree", "getTermCount", "getCoefficient", "addTerm", "add", "mult", "eval", "toString"};
	
	static int[] scoreWeight = {1, 1, 1, 1, 1, 1, 1, 0};
	
	static HashMap<String, Scorer> scorers = new HashMap<String, Scorer>();
	
	static Boolean verbose = true;
	
	static private void setOutputFile(String title) throws Exception {
		File file = new File(rootPath + title + ".txt");
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
	}
	
	static private HashMap<String, Integer> createWeightMap() {
		HashMap<String, Integer> weightedScore = new HashMap<String, Integer>();
		
		for (int i = 0; i < targetMethods.length; i++) {
			weightedScore.put(targetMethods[i], scoreWeight[i]);
		}
		
		return weightedScore;
	}
	
	static private ArrayList<String> getFolders(String subDir) {
		File root = new File(rootPath + subDir + "\\");
		
		ArrayList<String> students = new ArrayList<String>();
		File[] fileNameList = root.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		
		
		
		for (int i = 0; i < fileNameList.length; i++) {
			students.add(subDir + " " +fileNameList[i].getName());
		}
		return students;
	}
	
	static private String[] getStudentInfo(ArrayList<String> students, int tokenNum) {
		String[] output = new String[students.size()];
		
		for (int i = 0; i < students.size(); i++) {
			output[i] = students.get(i).split(" ")[tokenNum];
		}
		
		return output;
	}
	
	public static void main(String args[]) throws Exception {
		ArrayList<String> students = new ArrayList<String>();
		
		for (String subDir : classNum) {
			students.addAll(getFolders(subDir));
		}
		
		String[] stClass = getStudentInfo(students, 0);
		String[] stName = getStudentInfo(students, 1);
		String[] stId = getStudentInfo(students, 2);
		
		Scorer sc = new Scorer(rootPath, hwNum, stClass, stName, stId, pkgName, className, targetMethods, createWeightMap(), verbose);
		setOutputFile("Verbose");
		sc.test_all();
		setOutputFile("Scores");
		sc.printAllScore();
		System.exit(0);
	}
}


