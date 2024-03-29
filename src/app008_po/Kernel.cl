// created on Feb 7, 2013

/**
 * @author coglab
 */
//__constant sampler_t imageSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP | CLK_FILTER_NEAREST;
//__kernel void kernelcopy(
//    read_only image2d_t input, 
//    write_only image2d_t output
//    ) {
//    int2 coord = (int2)(get_global_id(0), get_global_id(1));
//    uint4 temp = read_imageui(input, imageSampler, coord);
//    write_imageui(output, coord, temp);
//}
//


__constant sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
 
__kernel void gaussian_blur(
        __read_only image2d_t image,
        __global int * output,
        __constant float * mask,
        __private int maskSize
    ) {
 
    const int2 pos = {get_global_id(0), get_global_id(1)};
    // Collect neighbor values and multiply with Gaussian
    float sum = 0.0f;
    for(int a = -maskSize; a < maskSize+1; a++) {
        for(int b = -maskSize; b < maskSize+1; b++) {
            sum += mask[a+maskSize+(b+maskSize)*(maskSize*2+1)]
                *read_imagef(image, sampler, pos + (int2)(a,b)).x;
        }
    }
 
    output[pos.x+pos.y*get_global_size(0)] = sum;
}