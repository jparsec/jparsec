package org.codehaus.jparsec.parameters;

import org.codehaus.jparsec.Parser.Mode;

/**
 * Runtime parameters applied to a parse execution.
 * @author Sylvain Colomer
 */
public class Parameters {
	private MapListener mapListener;
	private Mode mode = Mode.PRODUCTION;
	
	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public MapListener getMapListener() {
		return mapListener;
	}

	public void setMapListener(MapListener mapListener) {
		this.mapListener = mapListener;
	}
}
