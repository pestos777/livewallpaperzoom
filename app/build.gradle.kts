plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    namespace 'com.zoomearth.wallpaper'
    compileSdk 34

    defaultConfig {
        applicationId "com.zoomearth.wallpaper"
        minSdk 29
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }

    // Previene conflictos de archivos duplicados durante la empaquetación
    packaging {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
            excludes += 'META-INF/DEPENDENCIES'
        }
    }
}

// CORRECCIÓN DE CLASES DUPLICADAS (checkDebugDuplicateClasses)
configurations.all {
    resolutionStrategy {
        // Unifica las versiones de las librerías de Kotlin para evitar duplicados
        force "org.jetbrains.kotlin:kotlin-stdlib:1.9.22"
        force "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22"
        force "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.22"
        
        // Excluye la versión redundante de listenablefuture introducida por WorkManager/Guava
        exclude group: 'com.google.guava', module: 'listenablefuture'
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // WorkManager para la ejecución en segundo plano tras el desbloqueo
    implementation 'androidx.work:work-runtime-ktx:2.9.0'
}
