package com.smartvehicle.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "smv_image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private String schoolId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Lob
    @Column(name = "bytecode", nullable = false)
    private byte[] bytecode;

    @Column(name = "format", nullable = false, length = 10)
    private String format;

    @Column(name = "command", length = 255)
    private String command;

    @Column(name = "dev_id")
    private String devId;

    @Column(name = "veh_num", length = 50)
    private String vehNum;

    @Column(name = "detect_type")
    private Integer detectType;

    @Column(name = "reserve", length = 255)
    private String reserve;

    @Column(name = "ftp_path")
    private String ftpPath;

    public Image(String schoolId, String studentId, byte[] bytecode, String format, String command, String devId, String vehNum, Integer detectType, String reserve) {
        this.id = id;
        this.schoolId = schoolId;
        this.studentId = studentId;
        this.bytecode = bytecode;
        this.format = format;
        this.command = command;
        this.devId = devId;
        this.vehNum = vehNum;
        this.detectType = detectType;
        this.reserve = reserve;
    }

    // ✅ Required no-argument constructor for JPA
    public Image() {
    }


    // Optional constructor with all fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public byte[] getBytecode() {
        return bytecode;
    }

    public void setBytecode(byte[] bytecode) {
        this.bytecode = bytecode;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getVehNum() {
        return vehNum;
    }

    public void setVehNum(String vehNum) {
        this.vehNum = vehNum;
    }

    public Integer getDetectType() {
        return detectType;
    }

    public void setDetectType(Integer detectType) {
        this.detectType = detectType;
    }

    public String getReserve() {
        return reserve;
    }

    public void setReserve(String reserve) {
        this.reserve = reserve;
    }

    public String getFtpPath() {
        return ftpPath;
    }

    public void setFtpPath(String ftpPath) {
        this.ftpPath = ftpPath;
    }


}



