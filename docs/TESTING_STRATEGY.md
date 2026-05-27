# Testing Strategy

## Goals

MediaKit should prioritize:
- Stability
- Lifecycle safety
- Bitmap correctness
- Memory safety
- API reliability

## Recommended Testing Areas

### Image Picker
- Camera flows
- Permission denial
- Activity recreation
- Invalid URI handling
- Process death recovery

### Image Cropper
- Large image handling
- Crop accuracy
- Matrix transformations
- State restoration
- OOM prevention

## Future Improvements

- Automated UI tests
- Screenshot testing
- Instrumentation tests
- Memory stress testing
