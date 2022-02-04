package UncaughtExceptionHandlerEx;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UncaughtExceptionHandlerExample {
	
	// log4j2를 이용한 로거 생성(LoggerEx 참고)
	static Logger logger = LogManager.getLogger();
    
	public static void main(String[] args) {
		System.out.println("UncaughtExceptionHandlerExample 시작 - " + Thread.currentThread().getName());
		
		// 서비스가 종료될 때 실행될 문장
		Runtime.getRuntime().addShutdownHook(new Thread("ShutDown") {
		    public void run() {
			System.out.println("["+Thread.currentThread().getName()+"] Called");
			System.out.println("JVM 종료...");
		    }
		});

		// 처리하지 못한 예외 상황으로 인해 특정 스레드가 종료되는 시점에 호출되는 핸들러 설정(전체 스레드에 적용되는 기본 핸들러)
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
		    @Override
		    public void uncaughtException(Thread thread, Throwable e) {
			//System.out.println("DefaultUncaughtExceptionHanlder / " + thread + " throws exception: "+ e);
			logger.error("\nDefaultUncaughtExceptionHanlder / " + thread + " throws exception: ", e);
		    }
		});

		System.out.println("UncaughtExceptionHandlerExample 끝 - " + Thread.currentThread().getName());

		// 예외 만드는 스레드 생성 및 실행
		MakeExceptionThread te = new MakeExceptionThread("MakeExceptionThread");
		te.start();
	}
}
