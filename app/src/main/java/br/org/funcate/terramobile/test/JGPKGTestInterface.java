package br.org.funcate.terramobile.test;

/** Simple interface to receive a message from load and query tests.
 *
 */
public interface JGPKGTestInterface {
	/** A sets has been completed with the supplied text
	 * 
	 * @param msg
	 */
	public void testComplete(String msg);
}