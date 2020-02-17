package com.globile.santander.mobisec.logger;

import org.junit.Before;
import org.junit.Test;

public class GlobileLogTest {
    
    private String tag = "tag";
    private String message = "Log message";
    
    private Exception exception = new Exception();
    
    @Before
    public void setUp() throws Exception {
        GlobileLog.enable();
    }
    
    @Test
    public void v() {
        GlobileLog.v(message);
    }
    
    @Test
    public void v1() {
        GlobileLog.v(tag, message);
    }
    
    @Test
    public void v2() {
        GlobileLog.v(tag, message, exception);
    }
    
    @Test
    public void d() {
        GlobileLog.d(message);
    }
    
    @Test
    public void d1() {
        GlobileLog.d(tag, message);
    }
    
    @Test
    public void d2() {
        GlobileLog.d(tag, message, exception);
    }
    
    @Test
    public void i() {
        GlobileLog.i(message);
    }
    
    @Test
    public void i1() {
        GlobileLog.i(tag, message);
    }
    
    @Test
    public void i2() {
        GlobileLog.i(tag, message, exception);
    }
    
    @Test
    public void e() {
        GlobileLog.e(message);
    }
    
    @Test
    public void e1() {
        GlobileLog.e(exception);
    }
    
    @Test
    public void e2() {
        GlobileLog.e(message, exception);
    }
}