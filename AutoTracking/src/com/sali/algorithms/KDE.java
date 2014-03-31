package com.sali.algorithms;

import java.io.Serializable;

import android.database.Cursor;

/*
 * Instrumental class for KDE storage and management.
 * 
 * KDE distributions of RSSI's usually go from -40 to -100, with minor variations.
 * For sturdiness reasons it's stored as a 0..120 array index (with DEMAX = 120). 
 * 
 */
public class KDE implements Serializable {
	// Automatic created serial version for saved file (class) version control.
	private static final long serialVersionUID = 8577653431740491416L;
	// h = bandwidth
	private float h;
	// -DEMAX is expected to be the lowest value.
	private static final int DEMAX = 120;
	// KDE data distribution
	private float[] de = new float[DEMAX + 1];
	// Standard KDE for undiscovered wifi signals.
	public static final KDE kde = new KDE(new int[] { 100, 100, 100, 95 }, 0);

	/*
	 * Read the column values from the samplelist and use it as base for the
	 * KDE.
	 */
	public KDE(Cursor samplelist, int column) {
		int n = samplelist.getCount();
		int[] sample = new int[n];

		// Loading samples
		for (samplelist.moveToFirst(); !samplelist.isAfterLast(); samplelist
				.moveToNext()) {
			sample[samplelist.getPosition()] = samplelist.getInt(column);
		}

		recalulate(sample, n);
	}

	public KDE(int[] samplelist, int samplesize) {
		recalulate(samplelist, samplesize);
	}

	public KDE(Cursor samplelist, int column, float bandwidth) {
		recalulate(samplelist, column, bandwidth);
	}

	// power is minus the index of the array.
	public float prob(int power) {
		return de[Math.abs(power)];
	}

	public float bandwidth() {
		return h;
	}

	/*
	 * Calculate KDE from samples (and its size 'n') and return the found
	 * bandwidth value.
	 */
	public float recalulate(int[] sample, int n) {

		float[] kernel = new float[2 * DEMAX + 1];

		// Finding sub-optimal h
		float sm = 0;
		for (int i = 0; i < n; i++) {
			sm += sample[i];
		}
		sm /= n;

		float sc = 0;
		for (int i = 0; i < n; i++) {
			sc += (sample[i] - sm) * (sample[i] - sm);
		}
		try {
			sc /= (n - 1);
		} catch (ArithmeticException e) {
			sc /= n;
		}

		sc = (float) Math.pow((double) sc, 0.5);

		h = (float) (Math.pow(4.0 / (3 * n), 0.2) * sc);

		// Creating Kernel
		double a = 1.0 / (n * h * Math.pow(2 * Math.PI, 0.5));
		double b = 1.0 / Math.pow(Math.E, 1.0 / (2 * h * h));

		for (int i = 0; i < 2 * DEMAX + 1; i++)
			kernel[i] = (float) (a * Math.pow(b, (i - DEMAX) * (i - DEMAX)));

		// Creating DE (Kernel convolution)
		for (int p = 0; p < DEMAX + 1; p++)
			de[p] = 0;

		for (int i = 0; i < n; i++) {
			for (int p = 0; p < DEMAX + 1; p++)
				de[p] += kernel[DEMAX + p + sample[i]];

		}

		return h;
	}

	/*
	 * Calculate KDE from samples (and its size 'n') and a given bandwidth.
	 * Returning the bandwidth value.
	 */
	public float recalulate(Cursor samplelist, int column, float bandwidth) {
		h = Math.abs(bandwidth);
		int n = samplelist.getCount();
		float[] kernel = new float[2 * DEMAX + 1];
		int[] sample = new int[n];

		// Loading samples
		for (samplelist.moveToFirst(); !samplelist.isAfterLast(); samplelist
				.moveToNext()) {
			sample[samplelist.getPosition()] = samplelist.getInt(column);
		}

		// Creating Kernel
		double a = 1.0 / (n * h * Math.pow(2 * Math.PI, 0.5));
		double b = 1.0 / Math.pow(Math.E, 1.0 / (2 * h * h));

		for (int i = 0; i < 2 * DEMAX + 1; i++)
			kernel[i] = (float) (a * Math.pow(b, (i - DEMAX) * (i - DEMAX)));

		// Creating DE
		for (int p = 0; p < DEMAX + 1; p++)
			de[p] = 0;

		for (int i = 0; i < n; i++) {
			for (int p = 0; p < DEMAX + 1; p++)
				de[p] = kernel[DEMAX + p + sample[i]];

		}

		return h;

	}

}