package org.realityforge.webgpu;

import akasha.gpu.GPU;
import akasha.gpu.GPUAdapter;
import akasha.gpu.GPUCanvasConfiguration;
import akasha.gpu.GPUCanvasContext;
import akasha.gpu.GPUColorDict;
import akasha.gpu.GPUColorTargetState;
import akasha.gpu.GPUCommandBuffer;
import akasha.gpu.GPUCommandEncoder;
import akasha.gpu.GPUDevice;
import akasha.gpu.GPUExtent3DDict;
import akasha.gpu.GPUFragmentState;
import akasha.gpu.GPUPrimitiveState;
import akasha.gpu.GPUPrimitiveTopology;
import akasha.gpu.GPURenderPassColorAttachment;
import akasha.gpu.GPURenderPassDescriptor;
import akasha.gpu.GPURenderPassEncoder;
import akasha.gpu.GPURenderPipeline;
import akasha.gpu.GPURenderPipelineDescriptor;
import akasha.gpu.GPUShaderModuleDescriptor;
import akasha.gpu.GPUStoreOp;
import akasha.gpu.GPUTextureFormat;
import akasha.gpu.GPUTextureView;
import akasha.gpu.GPUVertexState;
import akasha.gpu.WGSL;
import elemental2.dom.Document;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;

/**
 * A simple port of https://github.com/austinEng/webgpu-samples/blob/main/src/sample/helloTriangle/main.ts
 */
@SuppressWarnings( "UnstableApiUsage" )
public final class Main
{
  private GPUDevice _device;
  private GPURenderPipeline _pipeline;
  private GPUCanvasContext _gl;
  private GPUAdapter _adapter;

  public void onModuleLoad()
  {
    Js.asPropertyMap( DomGlobal.navigator ).getAsAny( "gpu" ).<GPU>cast().requestAdapter().then( adapter -> {
      _adapter = adapter;
      return adapter.requestDevice();
    } ).thenAccept( this::onStart );
  }

  private void onStart( final GPUDevice device )
  {
    _device = device;
    final HTMLCanvasElement canvas = createCanvas();

    _gl = getGpuCanvasContext( canvas );

    // Use the preferred format of adapter instead of hardcoding to a specific format ala bgra8unorm.
    @GPUTextureFormat
    final String textureFormat = _gl.getPreferredFormat( _adapter );

    _gl.configure( GPUCanvasConfiguration
                     .create( _device, textureFormat )
                     //Ensure the configured size takes into account the device pixel ratio.
                     .size( calcGpuExtent3D( canvas ) ) );

    @WGSL
    final String vertexShader =
      "[[stage(vertex)]]\n" +
      "fn main([[builtin(vertex_index)]] VertexIndex : u32)\n" +
      "     -> [[builtin(position)]] vec4<f32> {\n" +
      "  var pos = array<vec2<f32>, 3>(\n" +
      "      vec2<f32>(0.0, 0.5),\n" +
      "      vec2<f32>(-0.5, -0.5),\n" +
      "      vec2<f32>(0.5, -0.5));\n" +
      "\n" +
      "  return vec4<f32>(pos[VertexIndex], 0.0, 1.0);\n" +
      "}";
    final GPUVertexState.Builder vertexState =
      GPUVertexState.create( _device.createShaderModule( GPUShaderModuleDescriptor.create( vertexShader ) ),
                             "main" );
    @WGSL
    final String fragmentShader =
      "[[stage(fragment)]]\n" +
      "fn main() -> [[location(0)]] vec4<f32> {\n" +
      "  return vec4<f32>(1.0, 0.0, 0.0, 1.0);\n" +
      "}";
    final GPUFragmentState fragmentState =
      GPUFragmentState.create( _device.createShaderModule( GPUShaderModuleDescriptor.create( fragmentShader ) ),
                               "main",
                               new GPUColorTargetState[]{ GPUColorTargetState.create( textureFormat ) } );

    _pipeline = _device.createRenderPipeline( GPURenderPipelineDescriptor
                                                .create( vertexState )
                                                .fragment( fragmentState )
                                                .primitive( GPUPrimitiveState
                                                              .create()
                                                              .topology( GPUPrimitiveTopology.triangle_list ) ) );

    DomGlobal.requestAnimationFrame( t -> renderFrame() );
  }

  private void renderFrame()
  {
    DomGlobal.requestAnimationFrame( t -> renderFrame() );
    final GPUCommandEncoder commandEncoder = _device.createCommandEncoder();
    final GPUTextureView textureView = _gl.getCurrentTexture().createView();

    final GPURenderPassColorAttachment attachment =
      GPURenderPassColorAttachment.create( textureView,
                                           GPUColorDict.create( 0, 0, 0, 1 ),
                                           GPUStoreOp.store );

    final GPURenderPassEncoder passEncoder =
      commandEncoder.beginRenderPass( GPURenderPassDescriptor.create( new GPURenderPassColorAttachment[]{ attachment } ) );
    passEncoder.setPipeline( _pipeline );
    passEncoder.draw( 3, 1, 0, 0 );
    passEncoder.endPass();

    _device.queue().submit( new GPUCommandBuffer[]{ commandEncoder.finish() } );
  }

  private static HTMLCanvasElement createCanvas()
  {
    final Document document = DomGlobal.document;
    final HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement( "canvas" );
    final HTMLElement body = Js.asPropertyMap( document ).getAsAny( "body" ).cast();
    body.appendChild( canvas );
    return canvas;
  }

  private static GPUExtent3DDict calcGpuExtent3D( final HTMLCanvasElement canvas )
  {
    final double devicePixelRatio = DomGlobal.self.devicePixelRatio;
    return GPUExtent3DDict
      .create( (int) ( canvas.clientWidth * devicePixelRatio ) )
      .height( (int) ( canvas.clientHeight * devicePixelRatio ) );
  }

  private static GPUCanvasContext getGpuCanvasContext( final HTMLCanvasElement canvas )
  {
    return Js.uncheckedCast( canvas.getContext( "webgpu" ) );
  }
}
