// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: NameNodeRpcServer.proto

package com.ybh.dfs.namenode.rpc.service;

public final class NameNodeServer {
  private NameNodeServer() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\027NameNodeRpcServer.proto\022\030com.ybh.dfs.n" +
      "amenode.rpc\032\026NameNodeRpcModel.proto2\364\006\n\017" +
      "NameNodeService\022a\n\010register\022).com.ybh.df" +
      "s.namenode.rpc.RegisterRequest\032*.com.ybh" +
      ".dfs.namenode.rpc.RegisterResponse\022d\n\the" +
      "artbeat\022*.com.ybh.dfs.namenode.rpc.Heart" +
      "beatRequest\032+.com.ybh.dfs.namenode.rpc.H" +
      "eartbeatResponse\022X\n\005mkdir\022&.com.ybh.dfs." +
      "namenode.rpc.MkdirRequest\032\'.com.ybh.dfs." +
      "namenode.rpc.MkdirResponse\022a\n\010shutdown\022)",
      ".com.ybh.dfs.namenode.rpc.ShutdownReques" +
      "t\032*.com.ybh.dfs.namenode.rpc.ShutdownRes" +
      "ponse\022p\n\rfetchEditsLog\022..com.ybh.dfs.nam" +
      "enode.rpc.FetchEditsLogRequest\032/.com.ybh" +
      ".dfs.namenode.rpc.FetchEditsLogResponse\022" +
      "\205\001\n\024updateCheckpointTxid\0225.com.ybh.dfs.n" +
      "amenode.rpc.UpdateCheckpointTxidRequest\032" +
      "6.com.ybh.dfs.namenode.rpc.UpdateCheckpo" +
      "intTxidResponse\022c\n\006create\022+.com.ybh.dfs." +
      "namenode.rpc.CreateFileRequest\032,.com.ybh",
      ".dfs.namenode.rpc.CreateFileResponse\022|\n\021" +
      "allocateDatanodes\0222.com.ybh.dfs.namenode" +
      ".rpc.AllocateDataNodesRequest\0323.com.ybh." +
      "dfs.namenode.rpc.AllocateDataNodesRespon" +
      "seB4\n com.ybh.dfs.namenode.rpc.serviceB\016" +
      "NameNodeServerP\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.ybh.dfs.namenode.rpc.model.NameNodeRpcModel.getDescriptor(),
        }, assigner);
    com.ybh.dfs.namenode.rpc.model.NameNodeRpcModel.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
