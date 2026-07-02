package cn.bugstack.middleware.dynamic.thread.pool.types;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private static final long serialVersionUID = -2474596551402989285L;

    /** 业务状态码，例如 0000 表示成功。 */
    private String code;

    /** 状态说明，给调用方或前端页面展示。 */
    private String info;

    /** 真正返回的数据，T 是泛型，占位表示“任意类型”。 */
    private T data;

    /**
     * Controller 统一使用的返回码枚举。
     *
     * <p>枚举可以避免在代码里到处写 "0000"、"0001" 这种魔法字符串。</p>
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum Code {
        /** 接口调用成功。 */
        SUCCESS("0000", "调用成功"),

        /** 未分类异常。 */
        UN_ERROR("0001", "调用失败"),

        /** 参数不合法。当前 Controller 里还没有实际使用这个状态。 */
        ILLEGAL_PARAMETER("0002", "非法参数"),
        ;

        /** 状态码。 */
        private String code;

        /** 状态说明。 */
        private String info;

    }

}

