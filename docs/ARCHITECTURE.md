# MediaKit Architecture

## Philosophy

MediaKit follows a modular Android SDK architecture.

Each media component should:
- Work independently
- Remain lightweight
- Minimize dependencies
- Expose clean APIs

## Current Modules

### imagepicker
Handles:
- Gallery selection
- Camera capture
- Lifecycle-safe media flows
- Crop integration

### ImageCropper
Handles:
- Crop overlays
- Matrix transformations
- Crop export logic
- Bitmap crop calculations

### sample-app
Used for:
- Integration testing
- SDK showcase
- Feature demos
- Manual QA

## Architectural Direction

MediaKit is evolving toward:
- Kotlin-first APIs
- Compose-friendly integrations
- Safer bitmap handling
- Production-ready media workflows
- Independently publishable modules
