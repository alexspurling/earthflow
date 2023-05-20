# Earthflow

This project interpolates images taken by NASA's DSCOVR satellite onto a 3D globe allowing a smooth rotation animation. It uses raytracing and reverse-projection to map the original 2D images onto a UV map and then blends them together to render them at any arbitrary point in time.

[animation.webm](https://github.com/alexspurling/earthflow/assets/137831/b919f3f1-b47e-4577-8002-66a12b3ca34b)

Future improvements: 
* Use the actual J2000 positions provided by the DSCOVR API to correctly position the camera with respect to the earth.
* Use a GPU shader to do the raytracing to improve performance