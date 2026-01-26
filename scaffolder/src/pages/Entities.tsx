import { ChevronDown, ChevronRight, Plus, Trash2 } from 'lucide-react';
import React, { useCallback, useState } from 'react';
import { EntityConfig, FieldConfig, FieldType, RelationshipConfig, RelationshipType } from '../models/types';
import { useProjectStore } from '../store/projectStore';

export const Entities: React.FC = () => {
  const { config, addEntity, updateEntity, removeEntity } = useProjectStore();
  const [collapsedEntityIndexes, setCollapsedEntityIndexes] = useState<Set<number>>(() => new Set());
  const [entityNameDrafts, setEntityNameDrafts] = useState<Record<number, string>>({});
  const [tableNameDrafts, setTableNameDrafts] = useState<Record<number, string>>({});
  const textInputClass =
    'px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:border-blue-500 focus:ring-blue-200';
  const textInputErrorClass =
    'px-3 py-2 border rounded-md focus:outline-none focus:ring-2 border-red-300 focus:border-red-400 focus:ring-red-200';
  const formLabelClass = 'block text-sm font-semibold text-gray-500 mb-1';
  const sectionTitleClass = 'text-sm font-semibold text-gray-600';

  const normalizeEntityName = useCallback((name: string) => name.trim().toLowerCase(), []);
  const normalizeTableName = useCallback((name: string) => name.trim().toLowerCase(), []);

  const isEntityNameDuplicate = useCallback(
    (entityIndex: number, candidateName: string) => {
      const candidate = normalizeEntityName(candidateName);
      if (!candidate) return false;
      for (let i = 0; i < config.entities.length; i += 1) {
        if (i === entityIndex) continue;
        const otherName = normalizeEntityName(entityNameDrafts[i] ?? config.entities[i]?.name ?? '');
        if (otherName === candidate) return true;
      }
      return false;
    },
    [config.entities, entityNameDrafts, normalizeEntityName],
  );

  const isTableNameDuplicate = useCallback(
    (entityIndex: number, candidateTableName: string) => {
      const candidate = normalizeTableName(candidateTableName);
      if (!candidate) return false;
      for (let i = 0; i < config.entities.length; i += 1) {
        if (i === entityIndex) continue;
        const otherTableName = normalizeTableName(tableNameDrafts[i] ?? config.entities[i]?.tableName ?? '');
        if (otherTableName === candidate) return true;
      }
      return false;
    },
    [config.entities, normalizeTableName, tableNameDrafts],
  );

  const clearEntityNameDraft = useCallback((entityIndex: number) => {
    setEntityNameDrafts((prev) => {
      if (!(entityIndex in prev)) return prev;
      const next = { ...prev };
      delete next[entityIndex];
      return next;
    });
  }, []);

  const clearTableNameDraft = useCallback((entityIndex: number) => {
    setTableNameDrafts((prev) => {
      if (!(entityIndex in prev)) return prev;
      const next = { ...prev };
      delete next[entityIndex];
      return next;
    });
  }, []);

  const shiftEntityNameDraftsOnDelete = useCallback((deletedIndex: number) => {
    setEntityNameDrafts((prev) => {
      if (Object.keys(prev).length === 0) return prev;
      const next: Record<number, string> = {};
      for (const [k, v] of Object.entries(prev)) {
        const i = Number(k);
        if (Number.isNaN(i)) continue;
        if (i < deletedIndex) next[i] = v;
        else if (i > deletedIndex) next[i - 1] = v;
      }
      return next;
    });
  }, []);

  const shiftTableNameDraftsOnDelete = useCallback((deletedIndex: number) => {
    setTableNameDrafts((prev) => {
      if (Object.keys(prev).length === 0) return prev;
      const next: Record<number, string> = {};
      for (const [k, v] of Object.entries(prev)) {
        const i = Number(k);
        if (Number.isNaN(i)) continue;
        if (i < deletedIndex) next[i] = v;
        else if (i > deletedIndex) next[i - 1] = v;
      }
      return next;
    });
  }, []);

  const createNewEntity = useCallback((): EntityConfig => {
    const { entities } = useProjectStore.getState().config;
    const normalize = (value: string) => value.trim().toLowerCase();
    const usedNames = new Set(entities.map((entity) => normalize(entity.name)));
    const usedTableNames = new Set(entities.map((entity) => normalize(entity.tableName)));

    let nextNumber = entities.length + 1;
    while (usedNames.has(normalize(`NewEntity${nextNumber}`)) || usedTableNames.has(normalize(`new_entity_${nextNumber}`))) {
      nextNumber += 1;
    }
    return {
      name: `NewEntity${nextNumber}`,
      tableName: `new_entity_${nextNumber}`,
      fields: [
        {
          name: 'id',
          type: 'long',
          primaryKey: true,
          nullable: false,
          unique: true,
        },
      ],
      relationships: [],
    };
  }, []);

  const startNewEntity = useCallback(() => {
    const newIndex = useProjectStore.getState().config.entities.length;
    addEntity(createNewEntity());
    setCollapsedEntityIndexes((prev) => {
      if (!prev.has(newIndex)) return prev;
      const next = new Set(prev);
      next.delete(newIndex);
      return next;
    });
  }, [addEntity, createNewEntity]);

  const toggleEntityCollapsed = useCallback((entityIndex: number) => {
    setCollapsedEntityIndexes((prev) => {
      const next = new Set(prev);
      if (next.has(entityIndex)) next.delete(entityIndex);
      else next.add(entityIndex);
      return next;
    });
  }, []);

  const setEntity = useCallback(
    (index: number, next: EntityConfig) => {
      updateEntity(index, next);
    },
    [updateEntity],
  );

  const addField = useCallback(
    (entityIndex: number) => {
      const entity = useProjectStore.getState().config.entities[entityIndex];
      if (!entity) return;
      const nextFieldNumber = entity.fields.length + 1;
      const nextField: FieldConfig = {
        name: `newField${nextFieldNumber}`,
        type: 'string',
        nullable: true,
        unique: false,
        primaryKey: false,
      };
      setEntity(entityIndex, { ...entity, fields: [...entity.fields, nextField] });
    },
    [setEntity],
  );

  const removeField = useCallback(
    (entityIndex: number, fieldIndex: number) => {
      const entity = useProjectStore.getState().config.entities[entityIndex];
      if (!entity) return;
      setEntity(entityIndex, { ...entity, fields: entity.fields.filter((_, i) => i !== fieldIndex) });
    },
    [setEntity],
  );

  const updateField = useCallback(
    (entityIndex: number, fieldIndex: number, patch: Partial<FieldConfig>) => {
      const entity = useProjectStore.getState().config.entities[entityIndex];
      if (!entity) return;
      const fields = [...entity.fields];
      fields[fieldIndex] = { ...fields[fieldIndex], ...patch };
      setEntity(entityIndex, { ...entity, fields });
    },
    [setEntity],
  );

  const addRelationship = useCallback(
    (entityIndex: number) => {
      const entity = useProjectStore.getState().config.entities[entityIndex];
      if (!entity) return;
      const nextRel: RelationshipConfig = { type: 'OneToMany', targetEntity: '', sourceField: '', targetField: '' };
      setEntity(entityIndex, { ...entity, relationships: [...entity.relationships, nextRel] });
    },
    [setEntity],
  );

  const updateRelationship = useCallback(
    (entityIndex: number, relIndex: number, patch: Partial<RelationshipConfig>) => {
      const entity = useProjectStore.getState().config.entities[entityIndex];
      if (!entity) return;
      const relationships = [...entity.relationships];
      relationships[relIndex] = { ...relationships[relIndex], ...patch };
      setEntity(entityIndex, { ...entity, relationships });
    },
    [setEntity],
  );

  const removeRelationship = useCallback(
    (entityIndex: number, relIndex: number) => {
      const entity = useProjectStore.getState().config.entities[entityIndex];
      if (!entity) return;
      setEntity(entityIndex, { ...entity, relationships: entity.relationships.filter((_, i) => i !== relIndex) });
    },
    [setEntity],
  );

  const deleteEntity = useCallback(
    (index: number) => {
      const ok = window.confirm('Are you sure you want to delete this entity? This action cannot be undone.');
      if (!ok) return;
      shiftEntityNameDraftsOnDelete(index);
      shiftTableNameDraftsOnDelete(index);
      setCollapsedEntityIndexes((prev) => {
        if (prev.size === 0) return prev;
        const next = new Set<number>();
        for (const i of prev) {
          if (i < index) next.add(i);
          else if (i > index) next.add(i - 1);
        }
        return next;
      });
      removeEntity(index);
    },
    [removeEntity, shiftEntityNameDraftsOnDelete, shiftTableNameDraftsOnDelete],
  );


  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-800">Entity Designer</h2>
        <button
          onClick={startNewEntity}
          className="flex items-center gap-1.5 bg-blue-600 text-white px-3 py-1.5 text-sm rounded-md hover:bg-blue-700 transition-colors"
        >
          <Plus size={16} />
          Add Entity
        </button>
      </div>

      <div className="grid gap-6">
        {config.entities.map((entity, entityIndex) => (
          <div key={entityIndex} className="bg-white border border-blue-200 shadow-md rounded-xl p-6 ring-2 ring-blue-100">
            <div className="flex items-start justify-between gap-4 mb-4">
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => toggleEntityCollapsed(entityIndex)}
                    className="text-gray-400 hover:text-gray-700"
                    aria-label={collapsedEntityIndexes.has(entityIndex) ? 'Expand entity' : 'Collapse entity'}
                  >
                    {collapsedEntityIndexes.has(entityIndex) ? <ChevronRight size={18} /> : <ChevronDown size={18} />}
                  </button>
                  <h3 className="font-semibold text-gray-800 truncate">{entity.name}</h3>
                </div>
              </div>
            </div>

            {collapsedEntityIndexes.has(entityIndex) ? null : (
              <>
                <div className="grid grid-cols-2 gap-4 mb-6">
              <div>
                <label className={formLabelClass}>Entity Name</label>
                <input
                  type="text"
                  value={entityNameDrafts[entityIndex] ?? entity.name}
                  onChange={(e) =>
                    setEntityNameDrafts((prev) => ({
                      ...prev,
                      [entityIndex]: e.target.value,
                    }))
                  }
                  onBlur={() => {
                    const draft = entityNameDrafts[entityIndex];
                    if (draft === undefined) return;
                    const nextName = draft.trim();
                    if (!nextName) return;
                    if (isEntityNameDuplicate(entityIndex, nextName)) return;
                    setEntity(entityIndex, { ...entity, name: nextName });
                    clearEntityNameDraft(entityIndex);
                  }}
                  className={`w-full ${
                    entityNameDrafts[entityIndex] !== undefined &&
                    isEntityNameDuplicate(entityIndex, entityNameDrafts[entityIndex])
                      ? textInputErrorClass
                      : textInputClass
                  }`}
                />
                {entityNameDrafts[entityIndex] !== undefined &&
                isEntityNameDuplicate(entityIndex, entityNameDrafts[entityIndex]) ? (
                  <div className="mt-1 text-xs text-red-600">Entity name already exists</div>
                ) : null}
              </div>
              <div>
                <label className={formLabelClass}>Table Name</label>
                <input
                  type="text"
                  value={tableNameDrafts[entityIndex] ?? entity.tableName}
                  onChange={(e) =>
                    setTableNameDrafts((prev) => ({
                      ...prev,
                      [entityIndex]: e.target.value,
                    }))
                  }
                  onBlur={() => {
                    const draft = tableNameDrafts[entityIndex];
                    if (draft === undefined) return;
                    const nextTableName = draft.trim();
                    if (!nextTableName) return;
                    if (isTableNameDuplicate(entityIndex, nextTableName)) return;
                    setEntity(entityIndex, { ...entity, tableName: nextTableName });
                    clearTableNameDraft(entityIndex);
                  }}
                  className={`w-full ${
                    tableNameDrafts[entityIndex] !== undefined &&
                    isTableNameDuplicate(entityIndex, tableNameDrafts[entityIndex])
                      ? textInputErrorClass
                      : textInputClass
                  }`}
                />
                {tableNameDrafts[entityIndex] !== undefined &&
                isTableNameDuplicate(entityIndex, tableNameDrafts[entityIndex]) ? (
                  <div className="mt-1 text-xs text-red-600">Table name already exists</div>
                ) : null}
              </div>
                </div>

            <div className="mb-6">
              <div className="flex justify-between items-center mb-2">
                <h4 className={sectionTitleClass}>Fields</h4>
                <button
                  onClick={() => addField(entityIndex)}
                  className="text-xs font-semibold flex items-center gap-1 text-blue-600 hover:text-blue-700"
                >
                  <Plus size={14} /> Add Field
                </button>
              </div>
              <div className="border rounded-lg overflow-hidden">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-3 py-2 text-left">Name</th>
                      <th className="px-3 py-2 text-left">Type</th>
                      <th className="px-3 py-2 text-center">Nullable</th>
                      <th className="px-3 py-2 text-center">Unique</th>
                      <th className="px-3 py-2 text-center">PK</th>
                      <th className="px-3 py-2"></th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {entity.fields.map((field, fieldIndex) => (
                      <tr key={fieldIndex}>
                        <td className="p-2">
                          <input
                            type="text"
                            value={field.name}
                            onChange={(e) => updateField(entityIndex, fieldIndex, { name: e.target.value })}
                            className={`w-full ${textInputClass}`}
                          />
                        </td>
                        <td className="p-2">
                          <select
                            value={field.type}
                            onChange={(e) => updateField(entityIndex, fieldIndex, { type: e.target.value as FieldType })}
                            className={`w-full ${textInputClass}`}
                          >
                            {['string', 'integer', 'long', 'boolean', 'decimal', 'date', 'datetime'].map((t) => (
                              <option key={t} value={t}>
                                {t}
                              </option>
                            ))}
                          </select>
                        </td>
                        <td className="p-2 text-center">
                          <input
                            type="checkbox"
                            checked={field.nullable}
                            onChange={(e) => updateField(entityIndex, fieldIndex, { nullable: e.target.checked })}
                          />
                        </td>
                        <td className="p-2 text-center">
                          <input
                            type="checkbox"
                            checked={field.unique}
                            onChange={(e) => updateField(entityIndex, fieldIndex, { unique: e.target.checked })}
                          />
                        </td>
                        <td className="p-2 text-center">
                          <input
                            type="checkbox"
                            checked={field.primaryKey}
                            onChange={(e) => updateField(entityIndex, fieldIndex, { primaryKey: e.target.checked })}
                          />
                        </td>
                        <td className="p-2 text-center">
                          {field.primaryKey && entity.fields.filter((candidate) => candidate.primaryKey).length === 1 ? null : (
                            <button
                              onClick={() => removeField(entityIndex, fieldIndex)}
                              className="text-red-500 hover:text-red-600"
                            >
                              <Trash2 size={16} />
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <div>
              <div className="flex justify-between items-center mb-2">
                <h4 className={sectionTitleClass}>Relationships</h4>
                <button
                  onClick={() => addRelationship(entityIndex)}
                  className="text-xs font-semibold flex items-center gap-1 text-blue-600 hover:text-blue-700"
                >
                  <Plus size={14} /> Add Relationship
                </button>
              </div>
              {entity.relationships.map((rel, relIndex) => (
                <div key={relIndex} className="flex gap-2 items-center mb-2 text-sm">
                  {(() => {
                    const normalizeValue = (value: string) => value.trim().toLowerCase();
                    const targetEntityMatch =
                      config.entities.find(
                        (candidate) => normalizeEntityName(candidate.name) === normalizeEntityName(rel.targetEntity),
                      ) ?? null;

                    const resolvedTargetEntityName = targetEntityMatch?.name ?? rel.targetEntity;
                    const targetEntityOptions = config.entities
                      .map((candidate) => candidate.name)
                      .sort((a, b) => a.localeCompare(b));
                    const sourceFieldOptions = entity.fields.map((field) => field.name).sort((a, b) => a.localeCompare(b));
                    const resolvedSourceFieldName =
                      sourceFieldOptions.find((name) => normalizeValue(name) === normalizeValue(rel.sourceField)) ??
                      rel.sourceField;

                    const effectiveTargetEntity =
                      targetEntityMatch ??
                      (resolvedTargetEntityName && normalizeValue(resolvedTargetEntityName) === normalizeValue(entity.name)
                        ? entity
                        : null);
                    const targetFieldOptions = (effectiveTargetEntity?.fields ?? [])
                      .map((field) => field.name)
                      .sort((a, b) => a.localeCompare(b));
                    const resolvedTargetFieldName =
                      targetFieldOptions.find((name) => normalizeValue(name) === normalizeValue(rel.targetField)) ??
                      rel.targetField;

                    return (
                      <>
                  <select
                    value={rel.type}
                    onChange={(e) => updateRelationship(entityIndex, relIndex, { type: e.target.value as RelationshipType })}
                    className={textInputClass}
                  >
                    <option value="OneToMany">OneToMany</option>
                    <option value="ManyToOne">ManyToOne</option>
                    <option value="ManyToMany">ManyToMany</option>
                  </select>
                  <span>to</span>
                  <select
                    value={resolvedTargetEntityName}
                    onChange={(e) =>
                      updateRelationship(entityIndex, relIndex, { targetEntity: e.target.value, targetField: '' })
                    }
                    className={textInputClass}
                  >
                    {resolvedTargetEntityName &&
                    !targetEntityOptions.some((name) => normalizeValue(name) === normalizeValue(resolvedTargetEntityName)) ? (
                      <option value={resolvedTargetEntityName}>{resolvedTargetEntityName}</option>
                    ) : null}
                    {targetEntityOptions.map((name) => (
                      <option key={name} value={name}>
                        {name}
                      </option>
                    ))}
                  </select>
                  <select
                    value={resolvedSourceFieldName}
                    onChange={(e) => updateRelationship(entityIndex, relIndex, { sourceField: e.target.value })}
                    className={textInputClass}
                  >
                    <option value="">Source Field</option>
                    {resolvedSourceFieldName &&
                    !sourceFieldOptions.some((name) => normalizeValue(name) === normalizeValue(resolvedSourceFieldName)) ? (
                      <option value={resolvedSourceFieldName}>{resolvedSourceFieldName}</option>
                    ) : null}
                    {sourceFieldOptions.map((name) => (
                      <option key={name} value={name}>
                        {name}
                      </option>
                    ))}
                  </select>
                  <select
                    value={resolvedTargetFieldName}
                    onChange={(e) => updateRelationship(entityIndex, relIndex, { targetField: e.target.value })}
                    className={textInputClass}
                    disabled={!resolvedTargetEntityName || targetFieldOptions.length === 0}
                  >
                    {resolvedTargetFieldName &&
                    !targetFieldOptions.some((name) => normalizeValue(name) === normalizeValue(resolvedTargetFieldName)) ? (
                      <option value={resolvedTargetFieldName}>{resolvedTargetFieldName}</option>
                    ) : null}
                    {targetFieldOptions.map((name) => (
                      <option key={name} value={name}>
                        {name}
                      </option>
                    ))}
                  </select>
                  <button onClick={() => removeRelationship(entityIndex, relIndex)} className="text-red-500 hover:text-red-600">
                    <Trash2 size={16} />
                  </button>
                      </>
                    );
                  })()}
                </div>
              ))}
            </div>

            <div className="pt-4 mt-6 border-t border-gray-200 flex justify-end">
              <button
                type="button"
                onClick={() => deleteEntity(entityIndex)}
                className="flex items-center gap-2 px-3 py-2 text-sm text-red-700 bg-red-50 hover:bg-red-100 rounded-lg"
              >
                <Trash2 size={18} />
                Remove Entity
              </button>
            </div>
              </>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
