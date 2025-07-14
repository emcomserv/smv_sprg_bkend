package com.smartvehicle.payload.response;

public class ImageResponseDTO {
    private Long id;
    private String schoolId;
    private String studentId;
    private String format;
    private String vehNum;
    private String command;
    private Integer detectType;
    private String reserve;
    private byte[] bytecode;
    private String ftpPath;

    public ImageResponseDTO(Long id, String schoolId, String studentId, String format, String vehNum,
                            String command, Integer detectType, String reserve, byte[] bytecode, String ftpPath) {
        this.id = id;
        this.schoolId = schoolId;
        this.studentId = studentId;
        this.format = format;
        this.vehNum = vehNum;
        this.command = command;
        this.detectType = detectType;
        this.reserve = reserve;
        this.bytecode = bytecode;
        this.ftpPath = ftpPath;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getVehNum() { return vehNum; }
    public void setVehNum(String vehNum) { this.vehNum = vehNum; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public Integer getDetectType() { return detectType; }
    public void setDetectType(Integer detectType) { this.detectType = detectType; }

    public String getReserve() { return reserve; }
    public void setReserve(String reserve) { this.reserve = reserve; }

    public byte[] getBytecode() { return bytecode; }
    public void setBytecode(byte[] bytecode) { this.bytecode = bytecode; }

    public String getFtpPath() { return ftpPath; }
    public void setFtpPath(String ftpPath) { this.ftpPath = ftpPath; }
}