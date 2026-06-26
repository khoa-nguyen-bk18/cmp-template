// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_analytics",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_analytics",
      type: .none,
      targets: ["_analytics"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/firebase/firebase-ios-sdk.git",
      from: "12.15.0"
    )
  ],
  targets: [
    .target(
      name: "_analytics",
      dependencies: [
        .product(
          name: "FirebaseCore",
          package: "firebase-ios-sdk"
        ),
        .product(
          name: "FirebaseAnalytics",
          package: "firebase-ios-sdk"
        ),
        .product(
          name: "FirebaseCrashlytics",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
