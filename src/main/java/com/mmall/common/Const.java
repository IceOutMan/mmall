package com.mmall.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.collect.Sets;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "eamil";
    public static final String USER_NAME = "username";

    public interface  Cart{
        int CHECKED = 1;//购物车选中
        int UN_CHECKED = 0;//购物车未选中

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";//限制失败
        String LIMIT_NUM_SUCCESS="LIMIT_NUM_SUCCESS";//限制成功
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    public interface Role{
        int ROLE_CUSTOMER = 0;//普通用户
        int ROLE_ADMIN = 1;//管理员
    }

    public enum ProductStatusEnum{
        ON_SALE(1,"在线");
        private String value;
        private int code;

        ProductStatusEnum(int code, String value){
            this.code = code;
            this.value = value;

        }

        public int getCode(){
            return code;
        }

        public String getValue(){
            return value;
        }
    }

    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");

        private String value;
        private int code;

        OrderStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static OrderStatusEnum codeOf(int code){
            for (OrderStatusEnum orderStatusEnum : values()){
                if (orderStatusEnum.getCode() == code) {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }


    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }


    public enum PayPlatFormEnum{
        ALIPAY(1,"支付宝");

        private String value;
        private int code;

        PayPlatFormEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付");

        private String value;
        private int code;

        PaymentTypeEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }


        public static PaymentTypeEnum codeOf(int code){
            for (PaymentTypeEnum paymentTypeEnum : values()){
                if (paymentTypeEnum.getCode() == code) {
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }

    }

}
