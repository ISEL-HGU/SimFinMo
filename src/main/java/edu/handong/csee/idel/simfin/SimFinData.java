package edu.handong.csee.idel.simfin;

public class SimFinData {
	private String key;
	private int rank;
	private double distance;
	private int label;
	private String similarChangeSHA;
	public String getSimilarChangeSHA() {
		return similarChangeSHA;
	}
	public void setSimilarChangeSHA(String similarChangeSHA) {
		this.similarChangeSHA = similarChangeSHA;
	}
	public String getSimilarChangePath() {
		return similarChangePath;
	}
	public void setSimilarChangePath(String similarChangePath) {
		this.similarChangePath = similarChangePath;
	}
	private String similarChangePath;
	
	public SimFinData(String key2, int rank2, Double distance2, int label2) {
		setKey(key2);
		setRank(rank2);
		setDistance(distance2);
		setLabel(label2);
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public int getLabel() {
		return label;
	}
	public void setLabel(int label) {
		this.label = label;
	}
}
