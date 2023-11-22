package cn.zhuhai.usercenter.model.enums;

/**
 * @Author Ewng
 * @Description 队伍状态枚举类
 * @Date 2023/11/22 10:55
 */
public enum TeamStatusEnum {
    PUBLIC (0, "公开"),
    PRIVATE (1, "私有"),
    SECRETE (2, "加密");


    private int value;

    private String text;

    /**
     * 根据传递的值获取对应的枚举
     * @param value 值
     * @return
     */
    public static TeamStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        // 获取枚举列表
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum status : values) {
            // 判断传递的值是否存在枚举列表中
            if (value == status.getValue()) {
                return status;
            }
        }
        return null;
    }

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 获取
     * @return value
     */
    public int getValue() {
        return value;
    }

    /**
     * 设置
     * @param value
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * 获取
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * 设置
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return "TeamStatusEnum{PUBLIC = " + PUBLIC + ", PRIVATE = " + PRIVATE + ", SECRETE = " + SECRETE + ", value = " + value + ", text = " + text + "}";
    }
}
