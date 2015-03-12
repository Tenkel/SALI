package com.sali.autotracking;

import java.io.Serializable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
	// Gaussian convertion from MAD to std dev
	private static final double K = 1.48260221850560186054707652936042343132670320259031289653626627524567444762269507362139420351582823911612666986905846932;
	// h = bandwidth
	private double h;
	// -DEMAX is expected to be the lowest value.
	private static final int DEMAX = 120;
	// LOG KDE data distribution
	private float[] log_de = new float[DEMAX + 1];
	// Standard KDE for undiscovered wifi signals.
	public static final float KDEstd = -200;
	public static final KDE kde = new KDE();
	
	

	/*
	 * Read the column values from the samplelist and use it as base for the
	 * KDE.
	 */
	private KDE(){
		this.h=0;
		for(int i=0;i<DEMAX+1;i++)
			log_de[i]=KDEstd;
	}
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
		return log_de[Math.abs(power)];
	}

	public double bandwidth() {
		return h;
	}

	/*
	 * Calculate KDE from samples (and its size 'n') and return the found
	 * bandwidth value.
	 */
	public double recalulate(int[] sample, int n) {

		double[] kernel = new double[2 * DEMAX + 1];

		// Finding sub-optimal h
		DescriptiveStatistics base_stats = new DescriptiveStatistics();

		for (int i = 0; i < n; i++) {
			sample[i] = Math.abs(sample[i]);
			base_stats.addValue(sample[i]);
		}

		double median = base_stats.getPercentile(50);

		DescriptiveStatistics dev_stats = new DescriptiveStatistics();

		for (int i = 0; i < n; i++)
			dev_stats.addValue(Math.abs(sample[i] - median));

		// "conversion" from MAD to std dev
		double sig = K * dev_stats.getPercentile(50);

		// if coulnd't compute, give some spread info
		if (sig <= 0)
			sig = base_stats.getMax() - base_stats.getMin();

		if (sig > 0) // Silverman rule of DUMB
			h = Math.pow(4.0 / (3 * n), 0.2) * sig;
		else
			// if everything goes wrong e.g. No std dev
			h = 1;

		// Creating Kernel
		double a = 1.0 / (n * h * Math.pow(2 * Math.PI, 0.5));
		double b = 1.0 / Math.pow(Math.E, 1.0 / (2 * h * h));

		for (int i = 0; i < 2 * DEMAX + 1; i++)
			kernel[i] = a * Math.pow(b, (i - DEMAX) * (i - DEMAX));

		// Creating DE (Kernel convolution)
		double[] de = new double[DEMAX + 1];

		for (int p = 0; p < DEMAX + 1; p++)
			de[p] = 0;

		for (int i = 0; i < n; i++)
			for (int p = 0; p < DEMAX + 1; p++)
				de[p] += kernel[DEMAX + p - sample[i]];

		for (int p = 0; p < DEMAX + 1; p++)
			log_de[p] = (float) Math.log(de[p]);

		return h;
	}

	/*
	 * Calculate KDE from samples (and its size 'n') and a given bandwidth.
	 * Returning the bandwidth value.
	 */
	public double recalulate(Cursor samplelist, int column, float bandwidth) {
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

		// Creating DE (Kernel convolution)
		double[] de = new double[DEMAX + 1];

		for (int p = 0; p < DEMAX + 1; p++)
			de[p] = 0;

		for (int i = 0; i < n; i++)
			for (int p = 0; p < DEMAX + 1; p++)
				de[p] += kernel[DEMAX + p + sample[i]];

		for (int p = 0; p < DEMAX + 1; p++)
			log_de[p] = (float) Math.log(de[p]);

		return h;

	}

}