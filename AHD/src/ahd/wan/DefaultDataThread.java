package ahd.wan;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

public class DefaultDataThread implements Runnable {
	
	public Buffer fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(30));
	
	public void run() {
		fifo.add(new Data());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.err.println("DefaultDataThread-run: "+e.getMessage());
		}
	}
	
}
