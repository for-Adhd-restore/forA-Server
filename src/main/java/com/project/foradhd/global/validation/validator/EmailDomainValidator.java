package com.project.foradhd.global.validation.validator;

import javax.naming.directory.*;
import javax.naming.Context;
import java.util.Hashtable;

public class EmailDomainValidator {

    /**
     * 주어진 도메인이 이메일을 수신할 수 있는지 MX 레코드 조회를 통해 검증
     */
    public static boolean isDomainValid(String domain) {
        try {
            // DNS 조회를 위한 환경 설정
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

            // DNS MX 레코드 조회
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
            Attribute attr = attrs.get("MX");

            return attr != null && attr.size() > 0; // MX 레코드가 존재하면 유효한 도메인
        } catch (Exception e) {
            return false; // 도메인이 존재하지 않거나 MX 레코드 조회 실패
        }
    }
}
