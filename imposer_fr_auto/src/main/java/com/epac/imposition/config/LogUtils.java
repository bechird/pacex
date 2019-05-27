package com.epac.imposition.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class LogUtils {
        
        private static final String DEFAULT_LOGGER = LogUtils.class.getName();
        
        private static final String START_METHOD_CHAR = "<";
        private static final String END_METHOD_CHAR   = ">";
        
        private static final Map<String, Logger> loggers = new HashMap<String,Logger>();
        
        public static Logger getLogger(){
                Logger logger = null;
                String name = getCallerId();
                
                if(name == null)
                        name = DEFAULT_LOGGER;
                
                if((logger = loggers.get(name)) == null)
                         loggers.put(name,(logger = Logger.getLogger(name)));
                
                return logger;
        }
        
        public static void start(){
                getLogger().debug(START_METHOD_CHAR);
        }
        
        public static void end(){
                getLogger().debug(END_METHOD_CHAR);
        }
        
        public static void debug(String message){
                getLogger().debug(message);
        }
        
        public static void info(String message){
                getLogger().info(message);
        }
        public static void warn(String message){
                getLogger().warn(message);
        }
        
        public static void warn(String message, Throwable t){
                getLogger().warn(message,t);
        }
        
        public static void error(String message){
                getLogger().error(message);
        }
        
        public static void error(String message, Throwable t){
                getLogger().error(message,t);
        }
        
        
        public static void fatal(String message, Throwable t){
                getLogger().fatal(message,t);
        }
        
        public static void fatal(String message){
                getLogger().fatal(message);
        }
        
        private static String getCallerId(){
                Throwable t = new Throwable();
                
                StackTraceElement[] stack = t.getStackTrace();
                
                
                if(stack.length > 3){
                        StackTraceElement e = stack[3];
                        return e.getClassName()+"::"+e.getMethodName();
                }
                return null;
        }

}

