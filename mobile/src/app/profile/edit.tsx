import { useRouter } from 'expo-router';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Button, Card, NumberField, ScrollScreen, Segmented, SelectField, Text, TextField, ToggleRow } from '@/components/ui';
import { CASTE_CATEGORIES, EDUCATION_LEVELS, INDIAN_STATES } from '@/constants/catalog';
import { useUserStore } from '@/store/userStore';
import type { CasteCategory, EducationLevel, EligibilityProfile, Gender } from '@/types/domain';

export default function EditProfileScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const stored = useUserStore((s) => s.profile);
  const save = useUserStore((s) => s.setProfile);
  const [p, setP] = useState<EligibilityProfile>(stored);
  const patch = (x: Partial<EligibilityProfile>) => setP((cur) => ({ ...cur, ...x }));

  return (
    <ScrollScreen>
      <Text variant="caption" tone="muted">
        {t('profile.eligibilityHint')}
      </Text>
      <Card>
        <TextField label={t('profile.name')} value={p.candidateName} onChangeText={(candidateName) => patch({ candidateName })} maxLength={80} autoCapitalize="words" />
        <NumberField label={t('profile.age')} value={p.age} min={14} max={70} onChange={(age) => patch({ age })} />
        <Text variant="caption" tone="muted">
          {t('profile.gender')}
        </Text>
        <Segmented<Gender>
          value={p.gender}
          onChange={(gender) => patch({ gender })}
          options={[
            { value: 'MALE', label: t('profile.male') },
            { value: 'FEMALE', label: t('profile.female') },
          ]}
        />
        <SelectField<CasteCategory>
          label={t('profile.category')}
          value={p.category}
          options={CASTE_CATEGORIES.map((c) => ({ value: c.key, label: `${c.label} (+${c.relaxationYears})` }))}
          onChange={(category) => patch({ category })}
        />
        <SelectField<EducationLevel>
          label={t('profile.qualification')}
          value={p.qualification}
          options={EDUCATION_LEVELS.map((e) => ({ value: e.key, label: e.label }))}
          onChange={(qualification) => patch({ qualification })}
        />
        <TextField label={t('profile.degree')} value={p.specificDegree} onChangeText={(specificDegree) => patch({ specificDegree })} maxLength={80} />
        <SelectField
          label={t('profile.homeState')}
          value={p.homeState}
          searchable
          options={INDIAN_STATES.map((s) => ({ value: s, label: s }))}
          onChange={(homeState) => patch({ homeState })}
        />
        <ToggleRow label={t('profile.pwd')} value={p.isPwD} onValueChange={(isPwD) => patch({ isPwD })} />
        <ToggleRow label={t('profile.exServicemen')} value={p.isExServicemen} onValueChange={(isExServicemen) => patch({ isExServicemen })} />
      </Card>

      <Card>
        <Text variant="subheading">{t('profile.physical')}</Text>
        <NumberField label={t('profile.height')} value={p.heightCm} min={100} max={230} decimals onChange={(heightCm) => patch({ heightCm })} />
        <NumberField label={t('profile.chestUnexpanded')} value={p.chestUnexpandedCm} min={50} max={150} decimals onChange={(chestUnexpandedCm) => patch({ chestUnexpandedCm })} />
        <NumberField label={t('profile.chestExpanded')} value={p.chestExpandedCm} min={50} max={160} decimals onChange={(chestExpandedCm) => patch({ chestExpandedCm })} />
      </Card>

      <Button
        label={t('common.save')}
        icon="checkmark"
        onPress={() => {
          save(p);
          if (router.canGoBack()) router.back();
        }}
      />
    </ScrollScreen>
  );
}
