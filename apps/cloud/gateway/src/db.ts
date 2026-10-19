import mongoose from 'mongoose';
import { Config } from './config.js';

let isConnected = false;

export async function connectDatabase(uri: string = Config.mongodbUri): Promise<typeof mongoose> {
  if (isConnected) return mongoose;

  mongoose.connection.on('connected', () => {
    isConnected = true;
    console.log('[Mongoose] Successfully connected to MongoDB database');
  });

  mongoose.connection.on('error', (err) => {
    console.error('[Mongoose] Connection error:', err);
  });

  mongoose.connection.on('disconnected', () => {
    isConnected = false;
    console.warn('[Mongoose] Disconnected from MongoDB. Reconnecting...');
  });

  try {
    await mongoose.connect(uri, {
      minPoolSize: 5,
      maxPoolSize: 25,
      serverSelectionTimeoutMS: 5000,
      socketTimeoutMS: 45000,
    });
    return mongoose;
  } catch (error) {
    console.warn('[Mongoose] Initial connection failed (offline mode):', error);
    return mongoose;
  }
}

export async function disconnectDatabase(): Promise<void> {
  if (isConnected) {
    await mongoose.disconnect();
    isConnected = false;
  }
}
