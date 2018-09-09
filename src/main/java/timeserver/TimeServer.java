package timeserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Netty时间服务器服务端
 * Created by fxd on 2018/9/9.
 */
public class TimeServer {

    public void bind(int port) throws Exception {
        /*
         * 配置服务端的NIO线程组
         * 专门用于网络事件的处理，实际上是Reactor线程组
         * 一个用于服务端接受客户端的链接
         * 一个用于进行SocketChannel的网络读写
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            /*
             * Netty用于启动NIO服务端的辅助启动类，目的是降低服务端的开发复杂度
             */
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //配置TCP参数
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //绑定I/O事件的处理类，主要用于处理网络I/O事件，例如记录日志、对消息进行编解码等
                    .childHandler(new ChildChannelHandler());

            // 绑定端口，同步等待成功
            // 调用同步阻塞方法sync等待绑定操作完成
            // ChannelFuture主要用于异步操作的通知回调
            ChannelFuture f = b.bind(port).sync();

            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // catch异常，使用默认值
            }
        }
        try {
            new TimeServer().bind(port);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
