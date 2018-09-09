package timeserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

/**
 * TimeServerHandler用于对网络事件进行读写操作
 * Created by fxd on 2018/9/9.
 */
public class TimeServerHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //类型转换
        ByteBuf buf = (ByteBuf) msg;
        //获取缓冲区可读的字节数，根据可读的字节数创建byte数组
        byte[] req = new byte[buf.readableBytes()];
        //将缓冲区中的字节数组复制到新建的byte数组中
        buf.readBytes(req);
        //获取请求消息
        String body = new String(req, "UTF-8");
        System.out.println("The time server receive order : " + body);

        //对请求消息进行判断
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ?
                new Date(System.currentTimeMillis()).toString() : "BAD ORDER";

        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        /*
         * 异步发送应答消息给客户端。从性能角度考虑，为了防止频繁地唤醒Selector进行消息发送
         * write方法并不直接将消息写入SocketChannel中，只是把待发送的消息放到发送缓冲数组中。
         */
        ctx.write(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //将消息发送队列中的消息写入到SocketChannel中发送给对方
        ctx.flush();
    }
}
