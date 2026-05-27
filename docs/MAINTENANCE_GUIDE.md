# Maintenance Guide

## Goals

MediaKit maintenance should focus on:
- Stability
- Backward compatibility
- Lightweight architecture
- Reliable media handling

## Recommended Maintenance Areas

### Image Picker
- Permission flow validation
- Lifecycle testing
- Camera integration testing
- URI handling verification

### Image Cropper
- Crop accuracy validation
- Bitmap memory monitoring
- Matrix transformation validation
- Large image testing

## Release Maintenance

Before releases:
- Verify CI builds
- Review public API changes
- Update changelog
- Validate sample app

## Long-Term Direction

MediaKit should evolve carefully while keeping APIs stable and modular.
