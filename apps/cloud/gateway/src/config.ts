import dotenv from 'dotenv';
dotenv.config();

export const Config = {
  port: parseInt(process.env.PORT || '4000', 10),
  mongodbUri: process.env.MONGODB_URI || 'mongodb://localhost:27017/dinevique_cloud',
  jwtSecret: process.env.JWT_SECRET || 'dinevique_dev_secret_key_2026',
  corsOrigin: process.env.CORS_ORIGIN || '*',
  environment: process.env.NODE_ENV || 'development',
};
