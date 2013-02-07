// created on Feb 7, 2013

/**
 * @author coglab
 */
__constant sampler_t imageSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP | CLK_FILTER_NEAREST;
__kernel void kernelcopy(
    read_only image2d_t input, 
    write_only image2d_t output
    ) {
    int2 coord = (int2)(get_global_id(0), get_global_id(1));
    uint4 temp = read_imageui(input, imageSampler, coord);
    write_imageui(output, coord, temp);
}