
import archiver from 'archiver';
import { FileNode } from '../models/types';
import { Writable } from 'stream';

export class ZipGeneratorService {
  public async generateZip(rootNode: FileNode, outputStream: Writable): Promise<void> {
    const archive = archiver('zip', {
      zlib: { level: 9 } // Sets the compression level.
    });

    archive.pipe(outputStream);

    this.addNodeToArchive(archive, rootNode, '');

    await archive.finalize();
  }

  private addNodeToArchive(archive: archiver.Archiver, node: FileNode, parentPath: string): void {
      // If node is root directory, we might want to skip adding it as a folder entry if we want its children at root of zip?
      // But typically zip contains the project folder.
      // Let's assume the rootNode IS the project folder.
      
      const currentPath = parentPath ? `${parentPath}/${node.name}` : node.name;

      if (node.type === 'file' && node.content !== undefined) {
          archive.append(node.content, { name: currentPath });
      } else if (node.type === 'directory') {
          // archiver doesn't explicitly require adding directories if we add files with paths, 
          // but empty directories might need it.
          // archive.directory(currentPath, false) or similar? 
          // Actually 'append' with name 'path/' creates a directory.
          // But if we iterate children recursively, files will have full paths.
          
          if (node.children && node.children.length > 0) {
              for (const child of node.children) {
                  this.addNodeToArchive(archive, child, parentPath ? currentPath : (parentPath === '' ? node.name : ''));
                  // Wait, logic above is slightly complex for recursion.
                  // Let's simplify:
                  // The node.path in FileNode is absolute-ish (e.g. "projectName/src/main/java").
                  // We can just use that relative to the root?
                  // Or we construct path during recursion.
              }
          } else {
              // Empty directory
              archive.append('', { name: currentPath + '/' });
          }
      }
  }
}
