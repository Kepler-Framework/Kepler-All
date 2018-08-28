package com.kepler;

/**
 * @author kim 2015年11月6日
 */
public class KeplerCodeException extends KeplerException {

    private static final long serialVersionUID = 1L;

    private int code;

    public KeplerCodeException(String e) {
        super(e);
        this.code = 1;
    }

    public KeplerCodeException(String e, int code) {
        super(e);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
