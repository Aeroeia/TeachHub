package com.teachub.authsdk.resource.filter;

import com.teachub.auth.common.constants.JwtConstants;
import com.teachub.common.utils.UserContext;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Activate(group = CommonConstants.PROVIDER)
public class DubboUserInfoFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String userIdStr = RpcContext.getContext().getAttachment(JwtConstants.USER_HEADER);
        if (userIdStr != null) {
            try {
                UserContext.setUser(Long.valueOf(userIdStr));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        try {
            return invoker.invoke(invocation);
        } finally {
            UserContext.removeUser();
        }
    }
}
