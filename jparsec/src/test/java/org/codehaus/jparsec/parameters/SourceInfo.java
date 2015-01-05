package org.codehaus.jparsec.parameters;

public class SourceInfo {
	private int start;
	private int end;
  private String filename;
	
  public SourceInfo(String filename, int start, int end) {
		super();
		this.filename = filename;
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

  public String getFilename() {
    return filename;
  }
}
