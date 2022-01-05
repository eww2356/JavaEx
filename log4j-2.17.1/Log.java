package OtherUtil;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class Log {
	// 로거 생성
	public static Logger log =  LogManager.getLogger(Log.class);
	File log4j2Properties = new File("config/log4j2.properties");
	// 로그 저장할 경로
	public static final String OS = System.getProperty("os.name").toLowerCase();
	public static final String win_path = "c:/projectPath";
	public static final String linux_path = "/home/User/projectPath";
	
	public Log() {
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
		
		// OS에 따른 log 기록 경로 가변 수정
		if (OS.indexOf("win") >= 0){
			System.setProperty("logFilename", win_path);
		}else{
			System.setProperty("logFilename", linux_path);
		}
		// log4j2 설정 경로
		loggerContext.setConfigLocation(log4j2Properties.toURI());
		// log4j2 설정 반영
		loggerContext.reconfigure();
	}
	
	// DEBUG 레벨이 너무 광범위한 것을 해결하기 위해서 좀 더 상세한 상태를 나타냄
	public void trace(String msg, Exception e) {
		log.trace(msg, e);
	}
	
	// 개발시 디버그 용도로 사용한 메시지
	public void debug(String msg, Exception e) {
		log.debug(msg, e);
	}
	
	// 정보성 메시지
	public void info(String msg, Exception e) {
		log.info(msg, e);
	}
	
	// 처리 가능한 문제이지만, 향후 시스템 에러의 원인이 될 수 있는 경고성 메시지
	public void warn(String msg, Exception e) {
		log.warn(msg, e);
	}
	
	// 아주 심각한 에러가 발생한 상태
	public void error(String msg, Exception e) {
		log.error(msg, e);
	}
	
	// 아주 심각한 에러
	public void fatal(String msg, Exception e) {
		log.fatal(msg, e);
	}

}
