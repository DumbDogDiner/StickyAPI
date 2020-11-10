package com.dumbdogdiner.stickyapi.bukkit.particle;

import org.bukkit.Particle;

/**
 * Represents a shape that can be drawn by a particle system.
 */
public interface Shape {
    /**
     * Draw this shape using relative co-ordinates. Implementations of this
     * method should ensure relative co-ordinates are being used by calling
     * {@link ParticleSystem#isAbsolute}.
     * @param system The particle system that is drawing the shape
     * @param particle The particle being used
     * @param data Extra particle data
     */
    void draw(ParticleSystem system, Particle particle, Particle.DustOptions data);
    
    /**
     * Draw this shape using absolute co-ordinates.
     * @param system The particle system that is drawing the shape
     * @param particle The particle being used
     * @param data Extra particle data
     */
    void drawAbsolute(ParticleSystem system, Particle particle, Particle.DustOptions data);
}
