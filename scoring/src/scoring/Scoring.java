package scoring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.ClassNotFoundException;

import classLoader.FileClassLoader;

class Main {
	static int[] testArray1 = {1, 3, 5, 7, 9};
	static int[] ansArray1 = {9, 7, 5, 3, 1};
	static int[] stNum = {2017012215, 2019123456};
	
	private static FileClassLoader fcLoader = new FileClassLoader();
	
	public static void test() throws Exception {
		ArrayList<Class<?>> classList = null;
		
		for (int stNum : stNum) {
			try {
				if (classList == null) {
					classList = fcLoader.getClass("C:\\Users\\mario\\Desktop\\hw1\\"  + stNum, new String[] {"InsertionSort"});
				}
				else {
					classList.addAll(fcLoader.getClass("C:\\Users\\mario\\Desktop\\hw1\\" + stNum, new String[] {"InsertionSort"}));
				}
			} catch(ClassNotFoundException e) {
				System.out.println(e);
			}
			
			Class<?> clazz = classList.get(classList.size()-1);
			Method m = clazz.getMethods()[0];
			
			int[] origin = testArray1.clone();
			m.invoke(clazz, testArray1);
			
			System.out.println(stNum + ": " + (Arrays.equals(testArray1, ansArray1) ? "Correct" : "Incorrect"));
			
			testArray1 = origin;
		}
	}
}

public class Scoring {
	public static void main(String args[]) throws Exception {
		Main.test();
	}
}


