package com.spring;

import com.plugin.annotationvalidate.hibernatevalidator.constraint.DateTimeRanges;
import com.plugin.annotationvalidate.hibernatevalidator.enums.Range;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zhangxu
 */
public class Car {

    /**
     * sfdsf
     */
    private String manufacturer;

    private String licensePlate;

    private int seatCount;

    @DateTimeRanges(range = Range.BEFORE)
    private Date date = new Date();

    public Car() throws ParseException {
        //date = new SimpleDateFormat("yyyy-MM-dd").parse("0000-00-00");
    }

    /**
     *
     * @return int
     */
    public int getSeatCount() {
        return seatCount;
    }

    /**
     *
     * @author lk
     * @date sds
     * @param seatCount

     * @return void
     */
    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

}
