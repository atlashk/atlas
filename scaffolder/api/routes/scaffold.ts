
import express from 'express';
import { ScaffoldController } from '../controllers/ScaffoldController';

const router = express.Router();
const controller = new ScaffoldController();

router.post('/validate', controller.validate);
router.get('/stack-options', controller.stackOptions);
router.post('/generate', controller.generate);
router.post('/preview', controller.preview);

export default router;
