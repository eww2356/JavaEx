package OtherUtil;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;


public class Log {
	// 로거 생성
	public static Logger log = Logger.getLogger(Log.class.getName());
	// 로그 저장할 경로
	public static final String OS = System.getProperty("os.name").toLowerCase();
	public static final String win_path = "c:/projectPath/";
	public static final String linux_path = "/home/User/projectPath/";
	
	public Log() {
		// OS에 따른 log 기록 경로 가변 수정
		Logger root = Logger.getRootLogger();
		@SuppressWarnings("unchecked")
		Enumeration<Appender> Appenders = root.getAllAppenders();
		FileAppender Output = (FileAppender) Appenders.nextElement();
		// assume there's only the one, and that it's a file appender
		if (OS.indexOf("win") >= 0){
			Output.setFile(win_path+"log/data.log");
		}else{
			Output.setFile(linux_path+"log/data.log");
		}
		Output.activateOptions();
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
