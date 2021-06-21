package com.github.saha.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class BlogClient {


    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client for Blog");

        BlogClient main = new BlogClient();
        main.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        String blogId = createBlog(channel);

        System.out.println("Reading blog....");
        readBlog(channel, blogId);
        System.out.println("Reading blog with non existing id....");
        readBlog(channel, "5bd83b5cf476074e2bff3495");

        updateBlog(channel, blogId);

        System.out.println("Deleting blog....");
        deleteBlog(channel, blogId);
        System.out.println("Deleting blog with non existing id....");
        deleteBlog(channel, "5bd83b5cf476074e2bff3495");

        listBlog(channel);
    }

    private String createBlog(ManagedChannel channel) {
        System.out.println("Creating blog");
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("Sumit")
                .setTitle("New blog!")
                .setContent("Hello world this is my first blog!")
                .build();

        CreateBlogResponse response = blogClient.createBlog(
                CreateBlogRequest.newBuilder()
                        .setBlog(blog)
                        .build()
        );

        System.out.println("Received create blog response");
        System.out.println(response.toString());
        return response.getBlog().getId();
    }

    private void readBlog(ManagedChannel channel, String blogId) {
        System.out.printf("Reading blog with blog id %s \n", blogId);
        try {
            BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
            ReadBlogResponse readBlogResponse = blogClient.readBlog(
                    ReadBlogRequest.newBuilder()
                            .setBlogId(blogId)
                            .build()
            );

            System.out.println(readBlogResponse.toString());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
    }

    private void updateBlog(ManagedChannel channel, String blogId) {
        Blog newBlog = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("Changed Author")
                .setTitle("New blog (updated)!")
                .setContent("Hello world this is my first blog! I've added some more content")
                .build();

        System.out.printf("Updating blog with blog id %s \n", blogId);
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        UpdateBlogResponse updateBlogResponse = blogClient.updateBlog(
                UpdateBlogRequest.newBuilder()
                        .setBlog(newBlog)
                        .build()
        );

        System.out.println("Updated blog");
        System.out.println(updateBlogResponse.toString());
    }

    private void deleteBlog(ManagedChannel channel, String blogId) {
        System.out.println("Deleting blog");
        try {
            BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
            DeleteBlogResponse deleteBlogResponse = blogClient.deleteBlog(
                    DeleteBlogRequest.newBuilder()
                            .setBlogId(blogId)
                            .build()
            );
            System.out.println("Deleted blog with blog id " + deleteBlogResponse.getBlogId());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
    }

    private void listBlog(ManagedChannel channel) {
        System.out.println("Listing blog");
        // we list the blogs in our database
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        blogClient.listBlog(
                ListBlogRequest.newBuilder()
                        .build()
        ).forEachRemaining(
                listBlogResponse -> System.out.println(listBlogResponse.getBlog().toString())
        );
    }
}
