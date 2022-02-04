package UncaughtExceptionHandlerEx;

// 에러 던지는 스레드
public class MakeExceptionThread extends Thread {
	
	// 스레드 이름을 설정하는 생성자
	public MakeExceptionThread(String threadName) {
		super(threadName);
	}

	@Override
	public void run() {
		System.out.println("ThrowException 시작 - " + Thread.currentThread().getName());
		
		// 처리된 예외
		try {
			String msg = "hoho";
			msg = msg.substring(5);
		} catch (Exception e) {
           		UncaughtExceptionHandlerExample.logger.error("\nCatch : ", e);
		}

		// 처리되지 않은 에러 만들기
		//throw new RuntimeException();
		// StringIndexOutOfBoundsException 만들기 위한 코드
		String msg = "sksk";
		msg = msg.substring(6);
		
		//System.out.println("ThrowException 끝 " + Thread.currentThread().getName());
	}
}
