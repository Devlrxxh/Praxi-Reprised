package me.funky.praxi.util;

public class MathHelper {

	public static int floor(double x) {
		int y = (int)x;
		return x < (double)y ? y - 1 : y;
	}

	public static int floor(float x) {
		int y = (int)x;
		return x < (float)y ? y - 1 : y;
	}

}
