/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI Frame tuple parsing.
 */
public class MIFrame {

	int level;
	long addr;
	String func = "";
	String file = "";
	int line;
	MIArg[] args = new MIArg[0];

	public MIFrame(MITuple tuple) {
		parse(tuple);
	}

	public MIArg[] getArgs() {
		return args;
	}

	public String getFile() {
		return file;
	}

	public String getFunction() {
		return func;
	}

	public int getLine() {
		return line;
	}

	public long getAddress() {
		return addr;
	}

	public int getLevel() {
		return level;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("level=\"" + level + "\"");
		buffer.append(",addr=\"" + Long.toHexString(addr) + "\"");
		buffer.append(",func=\"" + func + "\"");
		buffer.append(",file=\"" + file + "\"");
		buffer.append(",line=\"").append(line).append('"');
		buffer.append(",args=[");
		for (int i = 0; i < args.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append("{name=\"" + args[i].getName() + "\"");
			buffer.append(",value=\"" + args[i].getValue() + "\"}");
		}
		buffer.append(']');
		return buffer.toString();
	}

	void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = "";
			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getCString();
			}

			if (var.equals("level")) {
				try {
					level = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("addr")) {
				try {
					addr = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("func")) {
				func = ( str != null && str.equals( "??" ) ) ? "" : str;
			} else if (var.equals("file")) {
				file = str;
			} else if (var.equals("line")) {
				try {
					line = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("args")) {
				if (value instanceof MIList) {
					args = MIArg.getMIArgs((MIList)value);
				}
			}
		}
	}
}
