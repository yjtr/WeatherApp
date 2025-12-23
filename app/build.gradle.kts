plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.weatherapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.weatherapp"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 配置Room schema路径（工程内路径，可自定义）
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "${projectDir}/db/schemas" // 改成db/schemas，更贴合数据库目录
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 简化assets配置（默认src/main/assets即可，无需重复指定）
    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets") // 保留默认assets目录即可
        }
    }
}

// 1. 创建schema存储目录（db/schemas）
val createSchemaDir by tasks.registering {
    doLast {
        val schemaDir = file("${projectDir}/db/schemas")
        if (!schemaDir.exists()) {
            schemaDir.mkdirs()
            println("Schema directory created: ${schemaDir.absolutePath}")
        }
    }
}

// 2. 创建工程内db目录（用于存放预打包数据库，可选）
val createDbDir by tasks.registering {
    doLast {
        val dbDir = file("${projectDir}/db/source") // 工程内db/source放预打包db（若有）
        if (!dbDir.exists()) {
            dbDir.mkdirs()
            println("DB source directory created: ${dbDir.absolutePath}")
        }
    }
}

// 构建前执行目录创建任务
tasks.preBuild {
    dependsOn(createSchemaDir, createDbDir)
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 移除room-ktx（纯Java开发无需Kotlin扩展）
    implementation("androidx.room:room-runtime:2.5.0")
    annotationProcessor("androidx.room:room-compiler:2.5.0")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}